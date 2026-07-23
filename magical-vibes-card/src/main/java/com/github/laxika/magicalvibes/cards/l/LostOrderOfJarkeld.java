package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ICE", collectorNumber = "43")
public class LostOrderOfJarkeld extends Card {

    public LostOrderOfJarkeld() {
        // As this creature enters, choose an opponent.
        // Lost Order of Jarkeld's power and toughness are each equal to 1 plus the
        // number of creatures the chosen player controls.
        // In the two-player engine the chosen opponent is the single opponent
        // (Nyxathid / Canker Abomination precedent), so OPPONENTS scope counts
        // exactly the creatures that player controls.
        var pt = new Sum(
                new Fixed(1),
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.OPPONENTS));
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(pt, pt));
    }
}
