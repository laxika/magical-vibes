package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "92")
public class Corpseweft extends Card {

    public Corpseweft() {
        CreateTokenEffect zombieHorror = new CreateTokenEffect(
                "Zombie Horror",
                new Scaled(new XValue(), 2),
                new Scaled(new XValue(), 2),
                CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE, CardSubtype.HORROR),
                Set.of(), Set.of())
                .withTapped(true);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new ExileXCardsFromGraveyardCost(CardType.CREATURE, true),
                        zombieHorror
                ),
                "{1}{B}, Exile one or more creature cards from your graveyard: Create a tapped X/X black Zombie Horror creature token, where X is twice the number of cards exiled this way."
        ));
    }
}
