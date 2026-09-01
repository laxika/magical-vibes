package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardPutCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTargetCardFromGraveyardPutCounterOnTargetCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCardFromGraveyardPutCounterOnTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTargetCardFromGraveyardPutCounterOnTargetCreatureEffect) effect;
        List<UUID> graveyardTargets = entry.getTargetCardIdsForEffect(effect);
        if (graveyardTargets.isEmpty()) {
            graveyardTargets = entry.targetsForGroup(exileEffect.graveyardTargetGroup());
        }
        if (graveyardTargets.isEmpty()) {
            return;
        }

        UUID targetCardId = graveyardTargets.getFirst();
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null || !graveyardReturnSupport.exileCardFromAnyGraveyard(
                gameData, targetCardId, targetCard, entry.getSourcePermanentId())) {
            return;
        }
        if (!targetCard.hasType(CardType.CREATURE)) {
            return;
        }

        UUID creatureTargetId = entry.getTargetId();
        if (creatureTargetId == null) {
            List<UUID> creatureTargets = entry.targetsForGroup(exileEffect.creatureTargetGroup());
            creatureTargetId = creatureTargets.isEmpty() ? null : creatureTargets.getFirst();
        }
        if (creatureTargetId == null) {
            return;
        }
        Permanent targetCreature = gameQueryService.findPermanentById(gameData, creatureTargetId);
        if (targetCreature == null || !gameQueryService.isCreature(gameData, targetCreature)
                || !entry.getControllerId().equals(gameData.findControllerOf(targetCreature))) {
            return;
        }
        permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, targetCreature, 1);
    }
}
