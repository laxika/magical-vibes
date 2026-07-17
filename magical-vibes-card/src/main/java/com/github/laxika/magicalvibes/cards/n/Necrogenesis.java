package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "181")
public class Necrogenesis extends Card {

    public Necrogenesis() {
        // {2}: Exile target creature card from a graveyard. Create a 1/1 green Saproling creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD,
                                new CardTypePredicate(CardType.CREATURE)),
                        new CreateTokenEffect(1, "Saproling", 1, 1,
                                CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of())
                ),
                "{2}: Exile target creature card from a graveyard. Create a 1/1 green Saproling creature token."
        ));
    }
}
