package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyPlayerMayTakeDamageOrDrawTargetPlayerEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles one player's Browbeat damage-or-draw choice. */
@Component
public class AnyPlayerMayTakeDamageOrDrawTargetPlayerHandler implements MayEffectHandlerBean {

    private final AnyPlayerMayTakeDamageOrDrawTargetPlayerEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    public AnyPlayerMayTakeDamageOrDrawTargetPlayerHandler(
            AnyPlayerMayTakeDamageOrDrawTargetPlayerEffectHandler effectHandler,
            InputCompletionService inputCompletionService) {
        this.effectHandler = effectHandler;
        this.inputCompletionService = inputCompletionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect) ability.effects().getFirst();
        UUID chooserId = ability.controllerId();

        if (accepted) {
            effectHandler.dealDamage(gameData, ability, effect, chooserId);
        } else {
            List<UUID> remaining = effectHandler.remainingAfter(gameData, effect, chooserId);
            if (remaining.isEmpty()) {
                effectHandler.drawTargetPlayer(gameData, ability.sourceCard(), effect.abilityControllerId(),
                        ability.targetCardId(), effect.drawCount());
            } else {
                effectHandler.promptNext(gameData, ability.sourceCard(),
                        new AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect(
                                effect.damage(), effect.drawCount(), remaining, effect.abilityControllerId()),
                        ability.targetCardId());
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
