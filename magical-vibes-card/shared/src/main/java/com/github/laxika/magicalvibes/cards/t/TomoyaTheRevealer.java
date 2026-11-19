package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;

import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

public class TomoyaTheRevealer extends Card {

    public TomoyaTheRevealer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{U}{U}",
                List.of(new DrawCardEffect(new CardsInHand(CountScope.CONTROLLER), Subjects.targetPlayer())),
                "{3}{U}{U}, {T}: Target player draws X cards, where X is the number of cards in your hand."
        ));
    }
}
