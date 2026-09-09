package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "BFZ", collectorNumber = "180")
public class NissasRenewal extends Card {

    public NissasRenewal() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new Fixed(3), CardPredicateUtils.basicLand(),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(7));
    }
}
