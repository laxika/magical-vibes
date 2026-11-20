package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ARB", collectorNumber = "94")
public class MorbidBloom extends Card {

    public MorbidBloom() {
        // Exile target creature card from a graveyard, then create X 1/1 green Saproling creature
        // tokens, where X is the exiled card's toughness.
        addSpellEffect(ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(
                1, new CardTypePredicate(CardType.CREATURE), false));
        addSpellEffect(new CreateTokenEffect(1, "Saproling", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SAPROLING), Set.of(), Set.of())
                .withAmount(AmountThisWay.exiledToughness()));
    }
}
