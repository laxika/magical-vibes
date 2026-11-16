package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnSourceAuraToCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToCreatureOnDeathEffect;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Schedules the source Aura's next-end-step return after its enchanted creature dies. */
@Component
public class RegisterDelayedReturnSourceAuraToCreatureEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnSourceAuraToCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(Triggers.endStep(
                ActivePlayer.ANY), entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(), null,
                new ReturnSourceAuraToCreatureOnDeathEffect()));
    }
}
