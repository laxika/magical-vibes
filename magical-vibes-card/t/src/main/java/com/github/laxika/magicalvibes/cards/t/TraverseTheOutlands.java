package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "HBG", collectorNumber = "225")
@CardRegistration(set = "C17", collectorNumber = "34")
public class TraverseTheOutlands extends Card {

    public TraverseTheOutlands() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new GreatestPowerAmongControlled(),
                CardPredicateUtils.basicLand(),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
