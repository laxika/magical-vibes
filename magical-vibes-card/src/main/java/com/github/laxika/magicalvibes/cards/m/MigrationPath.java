package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "IKO", collectorNumber = "164")
public class MigrationPath extends Card {

    public MigrationPath() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new Fixed(2), CardPredicateUtils.basicLand(),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addCycling("{2}");
    }
}
