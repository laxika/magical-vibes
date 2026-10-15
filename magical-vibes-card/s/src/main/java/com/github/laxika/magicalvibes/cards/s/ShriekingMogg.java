package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "NEM", collectorNumber = "99")
public class ShriekingMogg extends Card {

    public ShriekingMogg() {
        // Haste is auto-loaded from Scryfall.
        // When this creature enters, tap all other creatures.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
    }
}
