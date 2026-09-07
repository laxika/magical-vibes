package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ExactlyAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessOfOtherAttackingCreatureEffect;

@CardRegistration(set = "VOW", collectorNumber = "151")
public class CreepyPuppeteer extends Card {

    public CreepyPuppeteer() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new ExactlyAttackers(2),
                new MayEffect(
                        new SetBasePowerToughnessOfOtherAttackingCreatureEffect(4, 3),
                        "Have the other attacking creature become 4/3 until end of turn?")));
    }
}
