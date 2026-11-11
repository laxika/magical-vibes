package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyTargetTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "247")
public class MirrorShieldHoplite extends Card {

    public MirrorShieldHoplite() {
        addTrigger(Triggers.creatureYouControlBecomesTargetOfBackupAbility(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CopyTargetTriggeredAbilityEffect());
    }
}
