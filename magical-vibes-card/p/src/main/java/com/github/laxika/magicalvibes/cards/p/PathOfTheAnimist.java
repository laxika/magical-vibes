package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.WillOfThePlaneswalkersEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "MOC", collectorNumber = "38")
@CardRegistration(set = "MOC", collectorNumber = "125")
public class PathOfTheAnimist extends Card {

    public PathOfTheAnimist() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new Fixed(2), CardPredicateUtils.basicLand(),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addEffect(EffectSlot.SPELL, new WillOfThePlaneswalkersEffect());
    }
}
