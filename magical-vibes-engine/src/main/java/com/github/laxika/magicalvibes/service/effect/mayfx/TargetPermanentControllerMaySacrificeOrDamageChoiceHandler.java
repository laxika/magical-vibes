package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerMaySacrificeOrDamageEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.TargetPermanentControllerMaySacrificeOrDamageEffectHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes a targeted permanent controller's sacrifice-or-damage choice. */
@Component
@RequiredArgsConstructor
public class TargetPermanentControllerMaySacrificeOrDamageChoiceHandler
        implements MayEffectHandlerBean {

    private final TargetPermanentControllerMaySacrificeOrDamageEffectHandler effectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPermanentControllerMaySacrificeOrDamageEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = ability.effects().stream()
                .filter(TargetPermanentControllerMaySacrificeOrDamageEffect.class::isInstance)
                .map(TargetPermanentControllerMaySacrificeOrDamageEffect.class::cast)
                .findFirst()
                .orElseThrow();
        effectHandler.resolveChoice(gameData, ability, accepted, effect);
    }
}
