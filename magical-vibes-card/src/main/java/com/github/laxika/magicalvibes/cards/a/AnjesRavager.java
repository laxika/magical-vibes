package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "C19", collectorNumber = "22")
public class AnjesRavager extends Card {

    public AnjesRavager() {
        // This creature attacks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        // Whenever this creature attacks, discard your hand, then draw three cards.
        addEffect(EffectSlot.ON_ATTACK, new DiscardOwnHandThenDrawEffect(new Fixed(3)));

        // Madness {1}{R}
        addCastingOption(new MadnessCast("{1}{R}"));
    }
}
