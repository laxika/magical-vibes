package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "THS", collectorNumber = "167")
public class NyleasDisciple extends Card {

    public NyleasDisciple() {
        // When this creature enters, you gain life equal to your devotion to green.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GainLifeEffect(new ColorManaSymbolsAmongControlledPermanents(ManaColor.GREEN)));
    }
}
