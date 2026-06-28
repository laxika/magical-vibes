package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerExilesTopCardsToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerExilesTopCardsToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerExilesTopCardsToSourceEffect) effect;
        // Find the source permanent (Knowledge Pool) on the battlefield
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = sourcePermanentId != null ? gameQueryService.findPermanentById(gameData, sourcePermanentId) : null;

        // Fallback: find permanent by card ID on controller's battlefield
        if (sourcePermanent == null) {
            UUID controllerId = entry.getControllerId();
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

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck == null) continue;

            int toExile = Math.min(e.count(), deck.size());
            List<String> exiledNames = new ArrayList<>();

            for (int i = 0; i < toExile; i++) {
                Card card = deck.removeFirst();
                exileService.exileCard(gameData, playerId, card, sourcePermanentId);
                exiledNames.add(card.getName());
            }

            if (!exiledNames.isEmpty()) {
                String playerName = gameData.playerIdToName.get(playerId);
                String logEntry = playerName + " exiles " + String.join(", ", exiledNames)
                        + " from the top of their library (" + sourcePermanent.getCard().getName() + ").";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} exiles {} cards from library to {}",
                        gameData.id, playerName, toExile, sourcePermanent.getCard().getName());
            }
        }
    }
}
