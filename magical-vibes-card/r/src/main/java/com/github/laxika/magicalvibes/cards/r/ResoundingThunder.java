package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "ALA", collectorNumber = "110")
public class ResoundingThunder extends Card {

    public ResoundingThunder() {
        // Resounding Thunder deals 3 damage to any target. (DealDamageToAnyTargetEffect's
        // TargetSpec narrows the legal target to any target — no explicit filter needed.)
        addSpellEffect(new DealDamageToAnyTargetEffect(3));

        // "When you cycle this card, it deals 6 damage to any target." The cycle trigger is separate
        // from the cycling draw, so its target fizzling never stops the draw, and it resolves first.
        addEffect(EffectSlot.ON_SELF_CYCLED, new DealDamageToAnyTargetEffect(6));

        addCycling("{5}{B}{R}{G}");
    }
}
