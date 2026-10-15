package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "101")
public class ShiftyDoppelganger extends Card {

    public ShiftyDoppelganger() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(
                        new ExileSelfCost(),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(
                                        new CardTypePredicate(CardType.CREATURE),
                                        "creature", false, false, true, true)
                                        .returningExiledSourceIfSacrificed(),
                                "Put a creature card from your hand onto the battlefield?"
                        )
                ),
                "{3}{U}, Exile Shifty Doppelganger: You may put a creature card from your hand onto "
                        + "the battlefield. If you do, that creature gains haste until end of turn. "
                        + "At the beginning of the next end step, sacrifice that creature. If you do, "
                        + "return this card to the battlefield."
        ));
    }
}
