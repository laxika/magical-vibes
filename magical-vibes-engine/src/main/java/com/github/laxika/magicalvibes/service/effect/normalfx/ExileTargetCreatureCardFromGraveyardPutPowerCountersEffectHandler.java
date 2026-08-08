package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardPutPowerCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handler for {@link ExileTargetCreatureCardFromGraveyardPutPowerCountersEffect} (Flesh): capture the
 * graveyard target's printed power, exile it, then put that many +1/+1 counters on the targeted
 * creature.
 *
 * <p>The two targets are handled independently (CR 608.2b): a graveyard card that already left its
 * graveyard makes X zero, and a creature that is no longer a legal target still leaves the exile
 * done.</p>
 */
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureCardFromGraveyardPutPowerCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureCardFromGraveyardPutPowerCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetCreatureCardFromGraveyardPutPowerCountersEffect) effect;

        List<UUID> graveyardGroup = entry.targetsForGroup(e.graveyardTargetGroup());
        int counters = 0;
        if (!graveyardGroup.isEmpty()) {
            UUID graveyardCardId = graveyardGroup.getFirst();
            Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, graveyardCardId);
            if (graveyardCard != null) {
                counters = graveyardCard.getPower() == null ? 0 : Math.max(0, graveyardCard.getPower());
                graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, graveyardCardId, graveyardCard);
                String playerName = gameData.playerIdToName.get(entry.getControllerId());
                gameLogService.append(gameData,
                        GameLog.textCardText(playerName + " exiles ", graveyardCard, " from a graveyard."));
            }
        }

        if (counters == 0) {
            return;
        }

        List<UUID> creatureGroup = entry.targetsForGroup(e.creatureTargetGroup());
        if (creatureGroup.isEmpty()) {
            return;
        }
        Permanent creature = gameQueryService.findPermanentById(gameData, creatureGroup.getFirst());
        if (creature == null) {
            return;
        }
        permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, creature, counters);
    }
}
