package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "568")
public class GertAndOldLaceRunaways extends Card {

    public GertAndOldLaceRunaways() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                DiscardCardThenEffect.continuing(
                        null,
                        new SearchLibraryEffect(CardPredicateUtils.basicLand()),
                        "a card"),
                "Discard a card to search your library for a basic land card?"));
    }
}
