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
    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCardFromGraveyardPutCounterOnTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTargetCardFromGraveyardPutCounterOnTargetCreatureEffect) effect;
        List<UUID> graveyardTargets = entry.getTargetCardIdsForEffect(effect);
        if (graveyardTargets.isEmpty()) {
            graveyardTargets = entry.getTargetCardIds();
        }
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

        queueReflexiveAbilityEffectHandler.resolve(gameData, entry,
                new com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect(
                        com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 1,
                                new com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate(List.of(
                                        new com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate(),
                                        new com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate())))));
    }
}
