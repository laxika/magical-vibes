package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "108")
public class NecromancersStockpile extends Card {

    public NecromancersStockpile() {
        // {1}{B}, Discard a creature card: Draw a card. If the discarded card was a Zombie card,
        // create a tapped 2/2 black Zombie creature token. The discard cost imprints the card it
        // paid with, so the Zombie check can inspect it at resolution.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.CREATURE), "creature",
                                false, 1, false, false, true),
                        new DrawCardEffect(1),
                        new ConditionalEffect(
                                new ImprintedCardMatches(new CardSubtypePredicate(CardSubtype.ZOMBIE), "a Zombie card",
                                        "discarded card"),
                                new CreateTokenEffect(1, "Zombie", 2, 2, CardColor.BLACK,
                                        List.of(CardSubtype.ZOMBIE), Set.of(), Set.of(), true)
                        )
                ),
                "{1}{B}, Discard a creature card: Draw a card. If the discarded card was a Zombie card, "
                        + "create a tapped 2/2 black Zombie creature token."
        ));
    }
}
