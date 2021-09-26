package org.example.app.handler;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.example.app.domain.Card;
import org.example.app.domain.User;
import org.example.app.dto.LoginRequestDto;
import org.example.app.dto.TransferRequestDto;
import org.example.app.exception.CardNotFoundException;
import org.example.app.exception.WrongAccessException;
import org.example.app.service.CardService;
import org.example.app.util.UserHelper;
import org.example.framework.attribute.RequestAttributes;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;

@Log
@RequiredArgsConstructor
public class CardHandler { // Servlet -> Controller -> Service (domain) -> domain
    private final CardService service;
    private final Gson gson;

    public void getAll(HttpServletRequest req, HttpServletResponse resp) {
        try {
            // cards.getAll?ownerId=1
            final var user = UserHelper.getUser(req);
            final List<Card> data;
            try {
                data = service.getAllByOwnerId(user, user.getId());
            } catch (WrongAccessException e) {
                resp.sendError(403);
                return;
            }
            resp.setHeader("Content-Type", "application/json");
            resp.getWriter().write(gson.toJson(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getById(HttpServletRequest req, HttpServletResponse resp) {
        log.log(Level.INFO, "getById");

        final var cardId = Long.parseLong(((Matcher) req.getAttribute(RequestAttributes.PATH_MATCHER_ATTR)).group("cardId"));
        final var user = UserHelper.getUser(req);
        try {
            try {
                final var card = service.getCardById(user, cardId);
                resp.setHeader("Content-Type", "application/json");
                resp.getWriter().write(gson.toJson(card));
            } catch (CardNotFoundException e) {
                resp.sendError(404);
            } catch (WrongAccessException e) {
                resp.sendError(403);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void order(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final var user = UserHelper.getUser(req);
            final var card = service.orderNewCard(user.getId());
            resp.setHeader("Content-Type", "application/json");
            resp.getWriter().write(gson.toJson(card));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void blockById(HttpServletRequest req, HttpServletResponse resp) {
        log.log(Level.INFO, "blockById");
        long cardId = Long.parseLong(req.getHeader("cardId"));

        final var user = UserHelper.getUser(req);
        try {
            try {
                service.blockById(user, cardId);
                resp.setStatus(200);
            } catch (CardNotFoundException e) {
                resp.sendError(404);
            } catch (WrongAccessException e) {
                resp.sendError(403);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void transfer(HttpServletRequest req, HttpServletResponse resp) {
        log.log(Level.INFO, "transfer");

        final TransferRequestDto requestDto;
        try {
            final var user = UserHelper.getUser(req);
            requestDto = gson.fromJson(req.getReader(), TransferRequestDto.class);
            try {
                final var responseDto = service.transfer(user, requestDto);
                resp.setHeader("Content-Type", "application/json");
                resp.getWriter().write(gson.toJson(responseDto));
            } catch (CardNotFoundException e) {
                resp.sendError(404);
            } catch (WrongAccessException e) {
                resp.sendError(403);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
