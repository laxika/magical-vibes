package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ETB handler: the controller exiles the top N cards of their own library face down,
 * tracked as "exiled with" the source permanent (Colfenor's Plans).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardsToSourceEffect) effect;
        UUID controllerId = entry.getControllerId();

        // Find the source permanent so exiled cards are tracked "with" it.
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = sourcePermanentId != null
                ? gameQueryService.findPermanentById(gameData, sourcePermanentId) : null;
        if (sourcePermanent == null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            if (bf != null) {
                for (Permanent p : bf) {
                    if (p.getCard().getId().equals(entry.getCard().getId())) {
                        sourcePermanent = p;
                        sourcePermanentId = p.getId();
                        break;
                    }
                }
            }
        }

        if (sourcePermanent == null) {
            log.info("Game {} - Source permanent no longer on battlefield, exile-top-cards fizzles", gameData.id);
            return;
        }

        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null) return;

        int toExile = Math.min(e.count(), deck.size());
        List<String> exiledNames = new ArrayList<>();
        for (int i = 0; i < toExile; i++) {
            Card card = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, card, sourcePermanentId);
            exiledNames.add(card.getName());
        }

        if (!exiledNames.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " exiles the top " + toExile + " card"
                    + (toExile != 1 ? "s" : "") + " of their library face down ("
                    + sourcePermanent.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(playerName + " exiles the top " + toExile + " card" + (toExile != 1 ? "s" : "") + " of their library face down (").card(sourcePermanent.getCard()).text(").").build());
            log.info("Game {} - {} exiles {} cards from library to {}",
                    gameData.id, playerName, toExile, sourcePermanent.getCard().getName());
        }
    }
}
