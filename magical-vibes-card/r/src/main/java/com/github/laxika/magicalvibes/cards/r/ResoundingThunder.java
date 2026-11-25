package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "110")
public class ResoundingThunder extends Card {

    public ResoundingThunder() {
        // Resounding Thunder deals 3 damage to any target. (DealDamageEffect's
        // TargetSpec narrows the legal target to any target — no explicit filter needed.)
        addSpellEffect(DealDamageEffect.toAnyTarget(3));

        // "When you cycle this card, it deals 6 damage to any target." The cycle trigger is separate
        // from the cycling draw, so its target fizzling never stops the draw, and it resolves first.
        addTrigger(Triggers.youCycleThisCard(), DealDamageEffect.toAnyTarget(6));

        addCycling("{5}{B}{R}{G}");
    }
}
