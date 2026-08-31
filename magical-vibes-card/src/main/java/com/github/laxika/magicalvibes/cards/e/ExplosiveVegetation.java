package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "ONS", collectorNumber = "263")
public class ExplosiveVegetation extends Card {

    public ExplosiveVegetation() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new Fixed(2), CardPredicateUtils.basicLand(),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
