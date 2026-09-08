package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "SCG", collectorNumber = "128")
public class SproutingVines extends Card {

    public SproutingVines() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
