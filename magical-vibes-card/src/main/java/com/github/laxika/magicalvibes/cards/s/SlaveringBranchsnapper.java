package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "198")
public class SlaveringBranchsnapper extends Card {

    public SlaveringBranchsnapper() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.FOREST))),
                "Forestcycling {2} ({2}, Discard this card: Search your library for a Forest card, "
                        + "reveal it, put it into your hand, then shuffle.)"
        ));
    }
}
