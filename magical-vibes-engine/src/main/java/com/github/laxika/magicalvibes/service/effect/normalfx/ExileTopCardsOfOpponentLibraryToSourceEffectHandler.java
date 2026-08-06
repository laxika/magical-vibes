package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfOpponentLibraryToSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Triggered handler: an opponent exiles the top N cards of their library, tracked "exiled with" the
 * source permanent (Grimoire Thief face down, Nightveil Specter face up). In a two-player game the
 * single opponent is the only legal target.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsOfOpponentLibraryToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsOfOpponentLibraryToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardsOfOpponentLibraryToSourceEffect) effect;
        UUID controllerId = entry.getControllerId();

        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = sourcePermanentId != null
                ? gameQueryService.findPermanentById(gameData, sourcePermanentId) : null;
        if (sourcePermanent == null) {
            log.info("Game {} - Source permanent no longer on battlefield, opponent-exile fizzles", gameData.id);
            return;
        }

        // A combat-damage trigger binds the damaged player as the target; otherwise (Grimoire
        // Thief) the single opponent is the only legal target in a two-player game.
        UUID opponentId = entry.getTargetId() != null && !entry.getTargetId().equals(controllerId)
                ? entry.getTargetId()
                : gameData.orderedPlayerIds.stream()
                        .filter(id -> !id.equals(controllerId))
                        .findFirst().orElse(null);
        if (opponentId == null) return;

        List<Card> deck = gameData.playerDecks.get(opponentId);
        if (deck == null) return;

        int toExile = Math.min(e.count(), deck.size());
        for (int i = 0; i < toExile; i++) {
            Card card = deck.removeFirst();
            if (e.faceDown()) {
                exileService.exileCardFaceDown(gameData, opponentId, card, sourcePermanentId);
            } else {
                exileService.exileCard(gameData, opponentId, card, sourcePermanentId);
            }
        }

        if (toExile > 0) {
            String playerName = gameData.playerIdToName.get(opponentId);
            String visibility = e.faceDown() ? " face down" : "";

            gameLogService.append(gameData, GameLog.builder().text(playerName + " exiles the top " + toExile + " card" + (toExile != 1 ? "s" : "") + " of their library" + visibility + " (").card(sourcePermanent.getCard()).text(").").build());
            log.info("Game {} - {} exiles {} cards from {}'s library to {}",
                    gameData.id, playerName, toExile, playerName, sourcePermanent.getCard().getName());
        }
    }
}
