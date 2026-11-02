package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "52")
@CardRegistration(set = "A25", collectorNumber = "103")
public class Ratcatcher extends Card {

    public Ratcatcher() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.RAT)),
                "Search your library for a Rat card?"));
    }
}
