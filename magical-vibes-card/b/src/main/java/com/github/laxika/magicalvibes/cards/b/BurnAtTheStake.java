package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "AVR", collectorNumber = "130")
public class BurnAtTheStake extends Card {

    public BurnAtTheStake() {
        // As an additional cost to cast this spell, tap any number of untapped creatures you control.
        addEffect(EffectSlot.SPELL, new TapAnyNumberOfPermanentsCost(new PermanentIsCreaturePredicate()));
        // Burn at the Stake deals damage to any target equal to three times the number of creatures
        // tapped this way — the tap cost snapshots that count into the spell's X value.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new Scaled(new XValue(), 3)));
    }
}
