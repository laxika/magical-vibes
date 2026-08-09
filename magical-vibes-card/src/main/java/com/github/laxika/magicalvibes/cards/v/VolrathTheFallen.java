package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "75")
public class VolrathTheFallen extends Card {

    public VolrathTheFallen() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.CREATURE), "creature",
                                false, 1, false, true),
                        new BoostSelfEffect(new XValue(), new XValue())
                ),
                "{1}{B}, Discard a creature card: Volrath the Fallen gets +X/+X until end of turn, "
                        + "where X is the discarded card's mana value."
        ));
    }
}
