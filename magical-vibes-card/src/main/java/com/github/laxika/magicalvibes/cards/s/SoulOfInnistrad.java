package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "115")
public class SoulOfInnistrad extends Card {

    public SoulOfInnistrad() {
        // {3}{B}{B}: Return up to three target creature cards from your graveyard to your hand.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}{B}",
                List.of(returnUpToThreeCreatureCards()),
                "{3}{B}{B}: Return up to three target creature cards from your graveyard to your hand."
        ));

        // {3}{B}{B}, Exile this card from your graveyard: Return up to three target creature cards
        // from your graveyard to your hand.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}{B}",
                List.of(new ExileSelfFromGraveyardCost(), returnUpToThreeCreatureCards()),
                "{3}{B}{B}, Exile this card from your graveyard: Return up to three target creature "
                        + "cards from your graveyard to your hand."
        ));
    }

    private static ReturnTargetCardsFromGraveyardToHandEffect returnUpToThreeCreatureCards() {
        return new ReturnTargetCardsFromGraveyardToHandEffect(new CardTypePredicate(CardType.CREATURE), 3);
    }
}
