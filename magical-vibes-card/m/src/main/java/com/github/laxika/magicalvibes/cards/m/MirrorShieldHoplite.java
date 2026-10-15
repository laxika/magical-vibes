package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyTargetTriggeredAbilityEffect;

@CardRegistration(set = "MOM", collectorNumber = "247")
public class MirrorShieldHoplite extends Card {

    public MirrorShieldHoplite() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_BACKUP_ABILITY,
                new CopyTargetTriggeredAbilityEffect());
    }
}
