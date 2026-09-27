package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "YMID", collectorNumber = "48")
public class GeistpackAlpha extends Card {

    public GeistpackAlpha() {
        // When Geistpack Alpha dies, seek a permanent card with mana value equal to the number
        // of lands you control.
        addEffect(EffectSlot.ON_DEATH, new SeekCardsToHandEffect(
                new Fixed(1),
                new CardIsPermanentPredicate(),
                new ManaValueBound(
                        new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER),
                        true,
                        0)));
    }
}
