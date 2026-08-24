package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "210")
public class TimberlandAncient extends Card {

    public TimberlandAncient() {
        // Reach and trample are intrinsic keywords (auto-loaded from Scryfall).
        // Forestcycling {2} searches for a Forest card and puts it into hand; discarding this card
        // is intrinsic to hand-activated abilities.
        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.FOREST))),
                "Forestcycling {2} ({2}, Discard this card: Search your library for a Forest card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
