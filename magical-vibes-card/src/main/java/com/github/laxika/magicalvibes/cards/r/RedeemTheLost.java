package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MOR", collectorNumber = "21")
public class RedeemTheLost extends Card {

    public RedeemTheLost() {
        // Target creature you control gains protection from the color of your choice until end of turn.
        // The clash resolves first so its won-clash return-to-hand flag is set before the protection
        // color choice interrupts resolution (spell disposition runs once resolution first breaks).
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control"))
                // Clash with an opponent. If you win, return Redeem the Lost to its owner's hand.
                .addEffect(EffectSlot.SPELL, new ClashEffect(ReturnToHandEffect.selfSpell()))
                .addEffect(EffectSlot.SPELL, new GrantProtectionChoiceUntilEndOfTurnEffect());
    }
}
