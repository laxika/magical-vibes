package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles one player's Breaking Point damage-or-destruction choice. */
@Component
public class AnyPlayerMayTakeDamageOrDestroyAllCreaturesHandler implements MayEffectHandlerBean {

    private final AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    public AnyPlayerMayTakeDamageOrDestroyAllCreaturesHandler(
            AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffectHandler effectHandler,
            InputCompletionService inputCompletionService) {
        this.effectHandler = effectHandler;
        this.inputCompletionService = inputCompletionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect) ability.effects().getFirst();
        UUID chooserId = ability.controllerId();

        if (accepted) {
            effectHandler.dealDamage(gameData, ability, effect, chooserId);
        } else {
            List<UUID> remaining = effectHandler.remainingAfter(gameData, effect, chooserId);
            if (remaining.isEmpty()) {
                effectHandler.destroyAllCreatures(gameData, ability.sourceCard(), effect.abilityControllerId());
            } else {
                effectHandler.promptNext(gameData, ability.sourceCard(),
                        new AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect(
                                effect.damage(), remaining, effect.abilityControllerId()));
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
