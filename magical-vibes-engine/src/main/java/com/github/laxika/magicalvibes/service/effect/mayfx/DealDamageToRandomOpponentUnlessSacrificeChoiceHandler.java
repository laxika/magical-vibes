package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentUnlessSacrificeEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToRandomOpponentUnlessSacrificeEffectHandler;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Resolves the random opponent's choice to sacrifice a nontoken creature or take damage. An accepted
 * sacrifice changes nothing before it (the opponent was picked when the prompt was made), so a
 * creature with competing death replacements is asked about before it leaves (CR 616.1) and the
 * answer is completed again with that choice.
 */
@Component
@RequiredArgsConstructor
public class DealDamageToRandomOpponentUnlessSacrificeChoiceHandler implements MayEffectHandlerBean {

    private final DealDamageToRandomOpponentUnlessSacrificeEffectHandler effectHandler;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

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
        if (accepted && effectHandler.hasLegalSacrifice(gameData, ability, effect)) {
            permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                    gameData, player, ability, true, () -> effectHandler.sacrifice(gameData, ability, effect));
            return;
        }
        effectHandler.dealDamageInstead(gameData, ability, effect);
    }
}
