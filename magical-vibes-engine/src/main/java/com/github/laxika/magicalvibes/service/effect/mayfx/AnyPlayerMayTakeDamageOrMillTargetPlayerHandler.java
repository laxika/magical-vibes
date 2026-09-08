package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayTakeDamageOrMillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyPlayerMayTakeDamageOrMillTargetPlayerEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles one player's Book Burning damage-or-mill choice. */
@Component
@RequiredArgsConstructor
public class AnyPlayerMayTakeDamageOrMillTargetPlayerHandler implements MayEffectHandlerBean {

    private final AnyPlayerMayTakeDamageOrMillTargetPlayerEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayTakeDamageOrMillTargetPlayerEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyPlayerMayTakeDamageOrMillTargetPlayerEffect) ability.effects().getFirst();
        UUID chooserId = ability.controllerId();

        if (accepted) {
            effectHandler.dealDamage(gameData, ability, effect, chooserId);
        } else {
            List<UUID> remaining = effectHandler.remainingAfter(gameData, effect, chooserId);
            if (remaining.isEmpty()) {
                effectHandler.millTargetPlayer(gameData, ability.sourceCard(), effect.abilityControllerId(),
                        ability.targetCardId(), effect.millCount());
            } else {
                effectHandler.promptNext(gameData, ability.sourceCard(),
                        new AnyPlayerMayTakeDamageOrMillTargetPlayerEffect(
                                effect.damage(), effect.millCount(), remaining, effect.abilityControllerId()),
                        ability.targetCardId());
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
