package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "183")
@CardRegistration(set = "GNT", collectorNumber = "58")
public class WelderAutomaton extends Card {

    public WelderAutomaton() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(DealDamageEffect.to(Subjects.eachOpponent(), 1)),
                "{3}{R}: This creature deals 1 damage to each opponent."
        ));
    }
}
