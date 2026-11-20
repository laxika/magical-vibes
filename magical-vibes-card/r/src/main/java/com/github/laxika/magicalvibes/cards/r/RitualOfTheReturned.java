package com.github.laxika.magicalvibes.cards.r;

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

@CardRegistration(set = "JOU", collectorNumber = "80")
public class RitualOfTheReturned extends Card {

    public RitualOfTheReturned() {
        // Exile target creature card from your graveyard. Create a black Zombie creature token.
        // Its power is equal to that card's power and its toughness is equal to that card's toughness.
        addSpellEffect(ExileGraveyardCardsEffect.oneTargetFromControllerGraveyard(
                new CardTypePredicate(CardType.CREATURE)));
        addSpellEffect(new CreateTokenEffect("Zombie", AmountThisWay.exiledPower().keepingNegative(),
                AmountThisWay.exiledToughness().keepingNegative(),
                CardColor.BLACK, List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()));
    }
}
