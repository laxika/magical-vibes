package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.SacrificeOtherCreatureOrDamageEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "Unless they sacrifice another creature of their choice" — the enchanted creature's controller may
 * sacrifice another creature instead of taking damage (Unnatural Hunger).
 */
@Component
@RequiredArgsConstructor
public class SacrificeOtherCreatureOrDamageHandler implements MayEffectHandlerBean {

    private final SacrificeOtherCreatureOrDamageEffectHandler sacrificeOrDamageEffectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOtherCreatureOrDamageEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        sacrificeOrDamageEffectHandler.resolveMayChoice(gameData, accepted, ability);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}
