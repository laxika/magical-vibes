package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "140")
public class PlaneboundAccomplice extends Card {

    public PlaneboundAccomplice() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardTypePredicate(CardType.PLANESWALKER),
                                "planeswalker", false, false, false, true),
                        "Put a planeswalker card from your hand onto the battlefield?"
                )),
                "{R}: You may put a planeswalker card from your hand onto the battlefield. "
                        + "Sacrifice it at the beginning of the next end step."
        ));
    }
}
