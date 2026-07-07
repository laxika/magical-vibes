package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "26")
public class ThrabenHeretic extends Card {

    public ThrabenHeretic() {
        // {T}: Exile target creature card from a graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD,
                        new CardTypePredicate(CardType.CREATURE))),
                "{T}: Exile target creature card from a graveyard."
        ));
    }
}
