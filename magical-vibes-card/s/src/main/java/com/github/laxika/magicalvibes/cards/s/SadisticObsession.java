package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "105")
public class SadisticObsession extends Card {

    public SadisticObsession() {
        target(TargetFilters.creature()).addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{B}",
                        List.of(new PutCountersEffect(CounterType.MINUS_ONE_MINUS_ONE)),
                        "{B}, {T}: Put a -1/-1 counter on target creature.",
                        TargetFilters.creature()
                ),
                Subjects.attachedCreature()
        ));
    }
}
