package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "218")
public class SneakAttack extends Card {

    public SneakAttack() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                "creature", false, false, true, true),
                        "Put a creature card from your hand onto the battlefield?"
                )),
                "{R}: You may put a creature card from your hand onto the battlefield. "
                        + "That creature gains haste until end of turn. Sacrifice the creature at the beginning of the next end step."
        ));
    }
}
