package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfOpponentLibraryToSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Triggered handler: target opponent exiles the top N cards of their library face down, tracked
 * "exiled with" the source permanent (Grimoire Thief). In a two-player game the single opponent is
 * the only legal target.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsOfOpponentLibraryToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
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

        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst().orElse(null);
        if (opponentId == null) return;

        List<Card> deck = gameData.playerDecks.get(opponentId);
        if (deck == null) return;

        int toExile = Math.min(e.count(), deck.size());
        for (int i = 0; i < toExile; i++) {
            Card card = deck.removeFirst();
            exileService.exileCardFaceDown(gameData, opponentId, card, sourcePermanentId);
        }

        if (toExile > 0) {
            String playerName = gameData.playerIdToName.get(opponentId);
            String logEntry = playerName + " exiles the top " + toExile + " card"
                    + (toExile != 1 ? "s" : "") + " of their library face down ("
                    + sourcePermanent.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(playerName + " exiles the top " + toExile + " card" + (toExile != 1 ? "s" : "") + " of their library face down (").card(sourcePermanent.getCard()).text(").").build());
            log.info("Game {} - {} exiles {} cards from {}'s library to {}",
                    gameData.id, playerName, toExile, playerName, sourcePermanent.getCard().getName());
        }
    }
}
