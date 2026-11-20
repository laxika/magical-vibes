package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "28")
public class ScourTheDesert extends Card {

    public ScourTheDesert() {
        // Exile target creature card from your graveyard. Create X 1/1 white Bird creature
        // tokens with flying, where X is the exiled card's toughness.
        addSpellEffect(ExileGraveyardCardsEffect.oneTargetFromControllerGraveyard(
                new CardTypePredicate(CardType.CREATURE)));
        addSpellEffect(new CreateTokenEffect("Bird", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of())
                .withAmount(AmountThisWay.exiledToughness()));
    }
}
