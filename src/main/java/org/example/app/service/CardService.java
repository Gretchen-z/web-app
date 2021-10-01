package org.example.app.service;

import lombok.RequiredArgsConstructor;
import org.example.app.domain.Card;
import org.example.app.domain.User;
import org.example.app.dto.TransferRequestDto;
import org.example.app.dto.TransferResponseDto;
import org.example.app.exception.CardNotFoundException;
import org.example.app.exception.WrongAccessException;
import org.example.app.repository.CardRepository;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import static org.example.framework.security.Roles.ROLE_ADMIN;

@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;

    public List<Card> getAllByOwnerId(User user, long ownerId) throws WrongAccessException {
        final var isAdmin = user.getRoles().stream().anyMatch(r -> r.equals(ROLE_ADMIN));

        if (isAdmin || user.getId() == ownerId) {
            return cardRepository.getAllByOwnerId(ownerId);
        }

        throw new WrongAccessException();
    }

    public Card getCardById(User user, long cardId) {
        final var card = cardRepository.getCardById(cardId).orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        final var cardOwnerId = card.getOwnerId();
        final var isAdmin = user.getRoles().stream().anyMatch(r -> r.equals(ROLE_ADMIN));

        if (isAdmin || user.getId() == cardOwnerId) {
            return card;
        }

        throw new WrongAccessException();
    }

    public void blockById(User user, long cardId) {
        final var card = cardRepository.getCardById(cardId).orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        final var cardOwnerId = card.getOwnerId();
        final var isAdmin = user.getRoles().stream().anyMatch(r -> r.equals(ROLE_ADMIN));

        if (isAdmin || user.getId() == cardOwnerId) {
            cardRepository.blockCardById(cardId);
            return;
        }

        throw new WrongAccessException();
    }

    public Card orderNewCard(long userId) {
        String newCardNumberString;
        do {
            SecureRandom rnd = new SecureRandom();
            int newCardNumber = rnd.nextInt(99999999);
            newCardNumberString = String.format("%08d", newCardNumber);
        } while (cardRepository.isCardNumberExist(newCardNumberString));

        return cardRepository.createCard(userId, newCardNumberString).orElseThrow(() -> new RuntimeException("Ошибка создания карты"));
    }

    public TransferResponseDto transfer(User user, TransferRequestDto transferOrder) {
        final var cardFrom = cardRepository.getCardByNumber(transferOrder.getCardNumFrom()).orElseThrow(() -> new CardNotFoundException("Карта не найдена"));
        final var cardTo = cardRepository.getCardByNumber(transferOrder.getCardNumTo()).orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        if (cardFrom.getOwnerId() != user.getId()) {
            throw new WrongAccessException();
        }

        final var amount = transferOrder.getAmount();

        final var transferResponseDto = new TransferResponseDto();
        transferResponseDto.setCardNumFrom(cardFrom.getNumber());
        transferResponseDto.setCardNumTo(cardTo.getNumber());
        transferResponseDto.setAmount(amount);

        if (amount <= 0 || cardFrom.getBalance() < amount) {
            transferResponseDto.setStatus("Отклонено");
            transferResponseDto.setReason(amount <= 0 ? "Сумма должна быть больше 0" : "Недостаточно средств");
            return transferResponseDto;
        }

        cardFrom.setBalance(cardFrom.getBalance() - amount);
        cardTo.setBalance(cardTo.getBalance() + amount);
        cardRepository.updateCardBalance(cardFrom.getId(), cardFrom.getBalance());
        cardRepository.updateCardBalance(cardTo.getId(), cardTo.getBalance());

        transferResponseDto.setStatus("Успешно");
        return transferResponseDto;
    }

}
