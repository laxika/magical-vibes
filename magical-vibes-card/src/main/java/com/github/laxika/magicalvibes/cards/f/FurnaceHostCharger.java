package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "140")
public class FurnaceHostCharger extends Card {

    public FurnaceHostCharger() {
        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.MOUNTAIN))),
                "Mountaincycling {2} ({2}, Discard this card: Search your library for a Mountain card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
