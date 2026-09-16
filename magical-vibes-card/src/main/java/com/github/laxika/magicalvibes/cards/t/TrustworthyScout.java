package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "33")
public class TrustworthyScout extends Card {

    public TrustworthyScout() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new SearchLibraryEffect(new CardNamedPredicate("Trustworthy Scout"))
                ),
                "{1}{W}, Exile this card from your graveyard: Search your library for a card named "
                        + "Trustworthy Scout, reveal it, put it into your hand, then shuffle."
        ));
    }
}
