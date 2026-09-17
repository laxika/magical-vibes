package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentUnlessSacrificeEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToRandomOpponentUnlessSacrificeEffectHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the choice to sacrifice a nontoken creature or take damage. */
@Component
@RequiredArgsConstructor
public class DealDamageToRandomOpponentUnlessSacrificeChoiceHandler implements MayEffectHandlerBean {

    private final DealDamageToRandomOpponentUnlessSacrificeEffectHandler effectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToRandomOpponentUnlessSacrificeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        DealDamageToRandomOpponentUnlessSacrificeEffect effect = ability.effects().stream()
                .filter(DealDamageToRandomOpponentUnlessSacrificeEffect.class::isInstance)
                .map(DealDamageToRandomOpponentUnlessSacrificeEffect.class::cast)
                .findFirst()
                .orElseThrow();
        effectHandler.resolveChoice(gameData, ability, accepted, effect);
    }
}
