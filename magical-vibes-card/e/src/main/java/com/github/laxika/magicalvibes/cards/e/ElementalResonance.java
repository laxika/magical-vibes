package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddManaEqualToEnchantedPermanentManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "83")
public class ElementalResonance extends Card {

    public ElementalResonance() {
        target(TargetFilters.permanent())
                .addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                        new AddManaEqualToEnchantedPermanentManaCostEffect());
    }
}
