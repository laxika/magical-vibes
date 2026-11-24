package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessGreaterThanPowerPredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "97")
public class ImmobilizerEldrazi extends Card {

    public ImmobilizerEldrazi() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{C}",
                List.of(new CantBlockThisTurnEffect(Subjects.narrowed(Subjects.allCreatures(),
                        new PermanentToughnessGreaterThanPowerPredicate()))),
                "{2}{C}: Each creature with toughness greater than its power can't block this turn."));
    }
}
