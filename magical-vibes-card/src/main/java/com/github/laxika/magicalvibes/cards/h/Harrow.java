package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "TMP", collectorNumber = "230")
public class Harrow extends Card {

    public Harrow() {
        // As an additional cost to cast this spell, sacrifice a land.
        addEffect(EffectSlot.SPELL, new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land"));

        // Search your library for up to two basic land cards, put them onto the battlefield, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new Fixed(2), CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD));
    }
}
