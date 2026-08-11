package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "123")
public class Unbury extends Card {

    public Unbury() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to your hand",
                        ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(
                                new CardTypePredicate(CardType.CREATURE))),
                new ChooseOneEffect.ChooseOneOption(
                        "Return two target creature cards that share a creature type from your graveyard to your hand",
                        ReturnTargetCardsFromGraveyardToHandEffect.exactlyTwoSharingCreatureType(
                                new CardTypePredicate(CardType.CREATURE)))
        )));
    }
}
