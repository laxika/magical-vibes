package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "MH1", collectorNumber = "181")
@CardRegistration(set = "SOC", collectorNumber = "287")
public class SpringbloomDruid extends Card {

    public SpringbloomDruid() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentIsLandPredicate(),
                        new SearchLibraryEffect(new Fixed(2), CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        "a land"),
                "Sacrifice a land?"));
    }
}
