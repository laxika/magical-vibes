package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedPlusOnePlusOneCounterRegrowthEffect;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Resolves the "whenever a +1/+1 counter is removed from this creature" ability of Protean Hydra: it
 * creates a delayed triggered ability that puts two +1/+1 counters on the creature at the beginning of the
 * next end step. The delayed trigger is controlled by the player who controlled this ability.
 */
@Component
public class DelayedPlusOnePlusOneCounterRegrowthEffectHandler implements NormalEffectHandlerBean {

    private static final int COUNTERS_PER_TRIGGER = 2;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DelayedPlusOnePlusOneCounterRegrowthEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.endStep(ActivePlayer.ANY), entry.getControllerId(), entry.getCard(),
                entry.getSourcePermanentId(), null, new PutCountersOnSourceEffect(1, 1, COUNTERS_PER_TRIGGER)));
    }
}
