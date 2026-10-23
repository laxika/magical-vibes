package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.SacrificeOtherCreatureOrDamageEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * "Unless they sacrifice another creature of their choice" — the enchanted creature's controller may
 * sacrifice another creature instead of taking damage (Unnatural Hunger). An accepted answer changes
 * nothing before it sacrifices a lone other creature (or opens the choice among several), so a
 * creature with competing death replacements is asked about before it leaves (CR 616.1) and the
 * answer is completed again with that choice; a declined one only deals the damage.
 */
@Component
@RequiredArgsConstructor
public class SacrificeOtherCreatureOrDamageHandler implements MayEffectHandlerBean {

    private final SacrificeOtherCreatureOrDamageEffectHandler sacrificeOrDamageEffectHandler;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOtherCreatureOrDamageEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                    gameData, player, ability, true, () -> resolve(gameData, true, ability));
        } else {
            resolve(gameData, false, ability);
        }
    }

    private void resolve(GameData gameData, boolean accepted, PendingMayAbility ability) {
        sacrificeOrDamageEffectHandler.resolveMayChoice(gameData, accepted, ability);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}
