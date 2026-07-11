package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "9ED", collectorNumber = "224")
@CardRegistration(set = "POR", collectorNumber = "152")
public class Thundermare extends Card {

    public Thundermare() {
        // Haste is auto-loaded from Scryfall.
        // When this creature enters, tap all other creatures.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
    }
}
