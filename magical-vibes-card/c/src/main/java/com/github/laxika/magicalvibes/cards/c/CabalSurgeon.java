package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "52")
public class CabalSurgeon extends Card {

    public CabalSurgeon() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}{B}",
                List.of(
                        new ExileNCardsFromGraveyardCost(2, null),
                        ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(
                                new CardTypePredicate(CardType.CREATURE))),
                "{2}{B}{B}, {T}, Exile two cards from your graveyard: Return target creature card "
                        + "from your graveyard to your hand."
        ));
    }
}
