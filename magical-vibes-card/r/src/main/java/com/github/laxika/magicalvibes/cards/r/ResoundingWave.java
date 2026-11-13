package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "54")
public class ResoundingWave extends Card {

    public ResoundingWave() {
        target(TargetFilters.permanent()).addSpellEffect(ReturnToHandEffect.target());

        // Cycling {5}{W}{U}{B}. "When you cycle this card, return two target permanents to their owners'
        // hands." Both targets are chosen as the trigger is put on the stack; with fewer than two legal
        // targets the trigger is removed and only the cycling draw happens.
        addCycling("{5}{W}{U}{B}");
        target(TargetFilters.permanent(), 2, 2).addTrigger(Triggers.youCycleThisCard(), ReturnToHandEffect.target());
    }
}
