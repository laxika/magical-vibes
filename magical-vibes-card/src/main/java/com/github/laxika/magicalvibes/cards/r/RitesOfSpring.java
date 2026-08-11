package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DiscardAnyNumberEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "ODY", collectorNumber = "265")
public class RitesOfSpring extends Card {

    public RitesOfSpring() {
        addEffect(EffectSlot.SPELL, new DiscardAnyNumberEffect());
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new EventValue(), CardPredicateUtils.basicLand(),
                LibrarySearchDestination.HAND));
    }
}
