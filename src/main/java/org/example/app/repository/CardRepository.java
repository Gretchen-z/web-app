package org.example.app.repository;

import lombok.RequiredArgsConstructor;
import org.example.app.domain.Card;
import org.example.jdbc.JdbcTemplate;
import org.example.jdbc.RowMapper;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CardRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Card> cardRowMapper = resultSet -> new Card(
            resultSet.getLong("id"),
            resultSet.getString("number"),
            resultSet.getLong("balance"),
            resultSet.getLong("ownerId")
    );

    public List<Card> getAllByOwnerId(long ownerId) {
        // language=PostgreSQL
        return jdbcTemplate.queryAll(
                "SELECT id, number, \"ownerId\", balance FROM cards WHERE \"ownerId\" = ? AND active = TRUE",
                cardRowMapper,
                ownerId
        );
    }

    public Optional<Card> getCardById(long cardId) {
        return jdbcTemplate.queryOne(
                // language=PostgreSQL
                "SELECT id, number, \"ownerId\", balance, \"ownerId\" FROM cards WHERE id = ? AND active = TRUE",
                cardRowMapper, cardId);
    }

    public Optional<Card> getCardByNumber(String cardNumber) {
        return jdbcTemplate.queryOne(
                // language=PostgreSQL
                "SELECT id, number, \"ownerId\", balance, \"ownerId\" FROM cards WHERE cards.number = ? AND active = TRUE",
                cardRowMapper, cardNumber);
    }

    public void blockCardById(long cardId) {
        jdbcTemplate.update(
                //language=PostgreSQL
                """
                        UPDATE cards
                        SET active=false
                        WHERE cards.id = ?
                        """, cardId);
    }

    public Optional<Card> createCard(long userId, String newCardNumber) {
        return jdbcTemplate.queryOne(
                //language=PostgreSQL
                """
                        INSERT INTO cards("ownerId", number, balance, active)
                        VALUES (?,  ?, 0, true)
                        RETURNING id, "ownerId", number, balance, active;
                        """, cardRowMapper, userId, newCardNumber);
    }

    public void updateCardBalance(long cardId, long newBalance) {
        jdbcTemplate.update(
                //language=PostgreSQL
                """
                        UPDATE cards
                        SET balance=?
                        WHERE cards.id = ?
                        """, newBalance, cardId);
    }


    public boolean isCardNumberExist(String cardNumber) {
        //language=PostgreSQL
        return jdbcTemplate.queryOne(
                "SELECT EXISTS(select 1 from cards where cards.number = ?);",
                rs -> rs.getBoolean(1),
                cardNumber)
                .orElse(false);
    }
}
