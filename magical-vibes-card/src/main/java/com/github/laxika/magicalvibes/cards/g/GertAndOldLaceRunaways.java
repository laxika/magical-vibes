package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "MSC", collectorNumber = "568")
public class GertAndOldLaceRunaways extends Card {

    public GertAndOldLaceRunaways() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        new SearchLibraryEffect(CardPredicateUtils.basicLand()),
                        "a card"),
                "Discard a card to search your library for a basic land card?"));
    }
}
