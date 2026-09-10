package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "165")
public class KessigWolfrider extends Card {

    public KessigWolfrider() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(
                        new ExileNCardsFromGraveyardCost(3, null),
                        new CreateTokenEffect("Wolf", 3, 2, CardColor.RED,
                                List.of(CardSubtype.WOLF), Set.of(), Set.of())
                ),
                "{2}{R}, {T}, Exile three cards from your graveyard: Create a 3/2 red Wolf creature token."
        ));
    }
}
