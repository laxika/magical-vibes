package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "RTR", collectorNumber = "134")
@CardRegistration(set = "KTK", collectorNumber = "150")
public class SeekTheHorizon extends Card {

    public SeekTheHorizon() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new Fixed(3), CardPredicateUtils.basicLand(),
                LibrarySearchDestination.HAND));
    }
}
