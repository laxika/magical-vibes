package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "129")
public class GreaterForgeling extends Card {

    public GreaterForgeling() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new BoostSelfEffect(3, -3)),
                "{1}{R}: This creature gets +3/-3 until end of turn."));
    }
}
