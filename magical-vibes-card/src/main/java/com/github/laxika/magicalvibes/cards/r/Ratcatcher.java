package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "DIS", collectorNumber = "52")
public class Ratcatcher extends Card {

    public Ratcatcher() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.RAT)),
                "Search your library for a Rat card?"));
    }
}
