package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MBS", collectorNumber = "5")
@CardRegistration(set = "5ED", collectorNumber = "27")
@CardRegistration(set = "MIR", collectorNumber = "11")
@CardRegistration(set = "CHR", collectorNumber = "6")
@CardRegistration(set = "LEG", collectorNumber = "9")
@CardRegistration(set = "ME4", collectorNumber = "10")
public class DivineOffering extends Card {

    public DivineOffering() {
        // Destroy target artifact. You gain life equal to its mana value.
        target(TargetFilters.artifact()).addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new GainLifeEffect(new TargetManaValue()));
    }
}
