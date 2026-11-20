package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "TLA", collectorNumber = "170")
public class CycleOfRenewal extends Card {

    public CycleOfRenewal() {
        addSpellEffect(new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), Subjects.you()));
        addSpellEffect(new SearchLibraryEffect(
                new Fixed(2), CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
