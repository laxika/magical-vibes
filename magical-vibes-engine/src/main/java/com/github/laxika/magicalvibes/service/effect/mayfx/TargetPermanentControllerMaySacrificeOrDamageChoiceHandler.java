package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerMaySacrificeOrDamageEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.TargetPermanentControllerMaySacrificeOrDamageEffectHandler;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Completes a targeted permanent controller's sacrifice-or-damage choice (Star Athlete). An
 * accepted sacrifice changes nothing before it, so a creature with competing death
 * replacements is asked about before it leaves (CR 616.1) and the answer is completed again with that
 * choice; a declined one only deals the damage.
 */
@Component
@RequiredArgsConstructor
public class TargetPermanentControllerMaySacrificeOrDamageChoiceHandler
        implements MayEffectHandlerBean {

    private final TargetPermanentControllerMaySacrificeOrDamageEffectHandler effectHandler;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

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
        if (accepted) {
            permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                    gameData, player, ability, true,
                    () -> effectHandler.resolveChoice(gameData, ability, true, effect));
        } else {
            effectHandler.resolveChoice(gameData, ability, false, effect);
        }
    }
}
