package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreLands;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "4ED", collectorNumber = "34")
public class LandTax extends Card {

    public LandTax() {
        // At the beginning of your upkeep, if an opponent controls more lands than you, you may
        // search your library for up to three basic land cards, reveal them, put them into your
        // hand, then shuffle.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(new OpponentControlsMoreLands(),
                new MayEffect(new SearchLibraryEffect(new Fixed(3), CardPredicateUtils.basicLand(),
                        LibrarySearchDestination.HAND),
                        "Search your library for up to three basic land cards?")));
    }
}
