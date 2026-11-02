package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyTargetTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "247")
public class MirrorShieldHoplite extends Card {

    public MirrorShieldHoplite() {
        addTrigger(Triggers.creatureYouControlBecomesTargetOfBackupAbility(),
                new CopyTargetTriggeredAbilityEffect());
    }
}
