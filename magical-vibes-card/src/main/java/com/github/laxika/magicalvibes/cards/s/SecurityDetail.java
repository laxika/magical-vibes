package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "47")
public class SecurityDetail extends Card {

    public SecurityDetail() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{W}",
                List.of(CreateTokenEffect.whiteSoldier(1)),
                "{W}{W}: Create a 1/1 white Soldier creature token. Activate only if you control no creatures and only once each turn.",
                1
        ).withActivationCondition(
                new ControlsPermanentCountAtMost(0, new PermanentIsCreaturePredicate()),
                "Activate only if you control no creatures."
        ));
    }
}
