package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Death-trigger resolution for Colfenor's Urn: exile the dying creature card from the graveyard it
 * went to and track it as "exiled with" the source permanent (via source permanent ID on the stack
 * entry), so the source can later count and return those cards.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTriggeringCreatureAndTrackWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringCreatureAndTrackWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTriggeringCreatureAndTrackWithSourceEffect) effect;
        UUID dyingCardId = e.dyingCardId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (dyingCardId == null || sourcePermanentId == null) {
            return;
        }

        // A card is put into its owner's graveyard — find it there and exile it, tracked with the source.
        for (Map.Entry<UUID, List<Card>> gy : gameData.playerGraveyards.entrySet()) {
            List<Card> graveyard = gy.getValue();
            for (Card card : graveyard) {
                if (card.getId().equals(dyingCardId)) {
                    UUID ownerId = gy.getKey();
                    graveyard.remove(card);
                    exileService.exileCard(gameData, ownerId, card, sourcePermanentId);
                    String logEntry = card.getName() + " is exiled with " + entry.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                    log.info("Game {} - {} exiled with {} (creature put into graveyard from battlefield)",
                            gameData.id, card.getName(), entry.getCard().getName());
                    return;
                }
            }
        }
    }
}
