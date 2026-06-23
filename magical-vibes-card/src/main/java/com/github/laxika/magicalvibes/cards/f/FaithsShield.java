package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeAtOrBelowThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceToControllerAndPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "DKA", collectorNumber = "7")
public class FaithsShield extends Card {

    public FaithsShield() {
        // The spell always targets a permanent you control (even during fateful hour).
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target must be a permanent you control"))
                // If you have 6 or more life: target permanent gains protection from the color of your choice.
                .addEffect(EffectSlot.SPELL, new ControllerLifeThresholdConditionalEffect(6,
                        new GrantProtectionChoiceUntilEndOfTurnEffect()))
                // Fateful hour — If you have 5 or less life, instead you and each permanent you control
                // gain protection from the color of your choice until end of turn.
                .addEffect(EffectSlot.SPELL, new ControllerLifeAtOrBelowThresholdConditionalEffect(5,
                        new GrantProtectionChoiceToControllerAndPermanentsUntilEndOfTurnEffect()));
    }
}
