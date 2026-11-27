package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.rules.ActionEvent;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Clashing with an opponent (CR 701.30), for the effects that clash. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClashSupport {

    private final GameLogService gameLogService;

    /**
     * Performs a clash for {@code clashingPlayerId} against their (single, 2-player) opponent
     * (CR 701.30): both players reveal the top card of their library, the clashing player
     * wins if their revealed card's mana value is strictly greater than the opponent's (CR 701.30d), and then
     * each player's clash is an {@link ActionEvent.Clashed} that "whenever you clash" abilities trigger on.
     *
     * <p>The caller subsequently offers each player a top-or-bottom placement choice for their
     * revealed card before continuing the clash effect.
     * The "whenever you clash" triggers fire after the clash ends. Invoked from a clash-source card's
     * effect resolution (see {@code ClashEffect}) or a test.
     *
     * @return {@code true} if the clashing player won the clash (their revealed card had a strictly
     *         greater mana value), so callers can apply an "if you won" reward.
     */
    public boolean performClash(GameData gameData, UUID clashingPlayerId) {
        return performClash(gameData, clashingPlayerId, gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(clashingPlayerId))
                .findFirst().orElse(null));
    }

    /**
     * {@link #performClash(GameData, UUID)} with the opponent {@code clashingPlayerId} chose to clash with ("clash with
     * an opponent" means "choose an opponent; you and that opponent each clash", CR 701.30b), or {@code null} for none.
     */
    public boolean performClash(GameData gameData, UUID clashingPlayerId, UUID opponentId) {
        Card clashingCard = topCard(gameData, clashingPlayerId);
        Card opponentCard = opponentId != null ? topCard(gameData, opponentId) : null;

        GameLog.Builder clashLog = GameLog.builder()
                .text(gameData.playerIdToName.get(clashingPlayerId) + " clashes: reveals ");
        if (clashingCard != null) {
            clashLog.card(clashingCard);
        } else {
            clashLog.text("no card (empty library)");
        }
        clashLog.text(opponentId == null ? "; no opponent reveals "
                : "; " + gameData.playerIdToName.get(opponentId) + " reveals ");
        if (opponentCard != null) {
            clashLog.card(opponentCard);
        } else {
            clashLog.text("no card (empty library)");
        }
        clashLog.text(".");
        gameLogService.append(gameData, clashLog.build());

        // CR 701.30d: win if your revealed card's mana value is higher than each other revealed card.
        boolean won = clashingCard != null
                && (opponentCard == null || clashingCard.getManaValue() > opponentCard.getManaValue());

        String outcome = won
                ? gameData.playerIdToName.get(clashingPlayerId) + " won the clash."
                : "No one won the clash.";
        gameLogService.append(gameData, GameLog.text(outcome));
        log.info("Game {} - {} clashes (won={})", gameData.id, clashingPlayerId, won);

        gameData.rulesEvents.occur(new ActionEvent.Clashed(clashingPlayerId, won));
        if (opponentId != null) {
            boolean opponentWon = opponentCard != null
                    && (clashingCard == null || opponentCard.getManaValue() > clashingCard.getManaValue());
            gameData.rulesEvents.occur(new ActionEvent.Clashed(opponentId, opponentWon));
        }
        return won;
    }

    private Card topCard(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        return (deck == null || deck.isEmpty()) ? null : deck.getFirst();
    }
}
