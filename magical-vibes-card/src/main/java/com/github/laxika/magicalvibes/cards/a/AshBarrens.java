package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "A25", collectorNumber = "236")
@CardRegistration(set = "2XM", collectorNumber = "310")
public class AshBarrens extends Card {

    public AshBarrens() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // Basic landcycling {1} ({1}, Discard this card: Search your library for a basic land card,
        // reveal it, put it into your hand, then shuffle.)
        addHandActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Basic landcycling {1} ({1}, Discard this card: Search your library for a basic land card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
