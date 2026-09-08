package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyPlayerMayTakeDamageOrTargetPlayerDrawEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one player's Browbeat damage choice. */
@Component
@RequiredArgsConstructor
public class AnyPlayerMayTakeDamageOrTargetPlayerDrawHandler implements MayEffectHandlerBean {

    private final AnyPlayerMayTakeDamageOrTargetPlayerDrawEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect) ability.effects().getFirst();
        if (accepted) {
            effectHandler.dealDamage(gameData, ability, effect, ability.controllerId());
        } else {
            effectHandler.advance(gameData, ability, effect, ability.controllerId());
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
