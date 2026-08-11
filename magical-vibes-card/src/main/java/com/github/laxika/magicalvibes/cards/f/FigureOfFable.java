package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureTypeWithBasePowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "224")
public class FigureOfFable extends Card {

    public FigureOfFable() {
        addActivatedAbility(new ActivatedAbility(false, "{G/W}",
                List.of(new BecomeCreatureTypeWithBasePowerToughnessEffect(2, 3, CardSubtype.SCOUT,
                        null, true)),
                "{G/W}: This creature becomes a Kithkin Scout with base power and toughness 2/3."));

        addActivatedAbility(new ActivatedAbility(false, "{1}{G/W}{G/W}",
                List.of(new BecomeCreatureTypeWithBasePowerToughnessEffect(4, 5, CardSubtype.SOLDIER,
                        CardSubtype.SCOUT, true)),
                "{1}{G/W}{G/W}: If this creature is a Scout, it becomes a Kithkin Soldier with base power and toughness 4/5."));

        addActivatedAbility(new ActivatedAbility(false, "{3}{G/W}{G/W}{G/W}",
                List.of(
                        new BecomeCreatureTypeWithBasePowerToughnessEffect(7, 8, CardSubtype.AVATAR,
                                CardSubtype.SOLDIER, true, true)
                ),
                "{3}{G/W}{G/W}{G/W}: If this creature is a Soldier, it becomes a Kithkin Avatar with base power and toughness 7/8 and protection from each of your opponents."));
    }
}
