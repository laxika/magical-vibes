package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.AnotherPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.SourceEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

public class MastersManufactory extends Card {

    public MastersManufactory() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(MastersGuideMural.golemToken()),
                "{T}: Create a 4/4 white and blue Golem artifact creature token. Activate only if this artifact "
                        + "or another artifact entered the battlefield under your control this turn."
        ).withActivationCondition(
                new AnyOf(List.of(
                        new SourceEnteredBattlefieldThisTurn(),
                        new AnotherPermanentEnteredThisTurn(new CardTypePredicate(CardType.ARTIFACT))
                )),
                "Activate only if this artifact or another artifact entered the battlefield under your control this turn"));
    }
}
