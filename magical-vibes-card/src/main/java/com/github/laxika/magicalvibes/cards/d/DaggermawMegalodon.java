package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "48")
public class DaggermawMegalodon extends Card {

    public DaggermawMegalodon() {
        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.ISLAND))),
                "Islandcycling {2} ({2}, Discard this card: Search your library for an Island card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
