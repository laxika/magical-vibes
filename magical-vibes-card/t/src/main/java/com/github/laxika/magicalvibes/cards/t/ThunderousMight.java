package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "114")
public class ThunderousMight extends Card {

    public ThunderousMight() {
        // The attack trigger is the Aura's own ability, so its controller (whose devotion counts) is the Aura's
        // controller even when the enchanted creature is an opponent's.
        setAttackTriggerOwnedByAura(true);

        target(TargetFilters.creature()).addTrigger(Triggers.attacks(),
                new BoostEquippedCreatureUntilEndOfTurnEffect(
                        new ColorManaSymbolsAmongControlledPermanents(ManaColor.RED),
                        new Fixed(0)));
    }
}
