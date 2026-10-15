package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "121")
public class EverbarkShaman extends Card {

    public EverbarkShaman() {
        // {T}, Exile a Treefolk card from your graveyard: Search your library for
        // up to two Forest cards, put them onto the battlefield tapped, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new ExileCardFromGraveyardCost(CardSubtype.TREEFOLK),
                        new SearchLibraryEffect(
                                new Fixed(2),
                                new CardSubtypePredicate(CardSubtype.FOREST),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                "{T}, Exile a Treefolk card from your graveyard: Search your library for up to two Forest cards, put them onto the battlefield tapped, then shuffle."
        ));
    }
}
