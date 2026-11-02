package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C19", collectorNumber = "22")
@CardRegistration(set = "VOC", collectorNumber = "141")
public class AnjesRavager extends Card {

    public AnjesRavager() {
        // This creature attacks each combat if able.
        addStaticEffect(new MustAttackEffect());

        // Whenever this creature attacks, discard your hand, then draw three cards.
        addTrigger(Triggers.attacks(), new DiscardOwnHandThenDrawEffect(new Fixed(3)));

        // Madness {1}{R}
        addCastingOption(new MadnessCast("{1}{R}"));
    }
}
