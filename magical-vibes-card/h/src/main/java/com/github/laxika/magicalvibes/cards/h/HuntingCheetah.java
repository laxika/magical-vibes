package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PTK", collectorNumber = "138")
@CardRegistration(set = "ME3", collectorNumber = "124")
@CardRegistration(set = "MB2", collectorNumber = "134")
public class HuntingCheetah extends Card {

    public HuntingCheetah() {
        addTrigger(Triggers.dealsDamageToOpponent(),
                new MayEffect(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.FOREST)),
                        "Search your library for a Forest card?"));
    }
}
