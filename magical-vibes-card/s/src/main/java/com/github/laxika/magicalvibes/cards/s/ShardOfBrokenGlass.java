package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "262")
public class ShardOfBrokenGlass extends Card {

    public ShardOfBrokenGlass() {
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature()));
        addTrigger(Triggers.attacks(),
                new MayEffect(new MillEffect(2, Subjects.you()), "Mill two cards?"));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
