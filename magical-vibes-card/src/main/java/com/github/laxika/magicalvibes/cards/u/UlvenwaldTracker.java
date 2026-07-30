package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "200")
public class UlvenwaldTracker extends Card {

    public UlvenwaldTracker() {
        // {1}{G}, {T}: Target creature you control fights another target creature.
        // "another" is covered by the default global target uniqueness of multi-target abilities.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}",
                List.of(new FightTargetsEffect()),
                "{1}{G}, {T}: Target creature you control fights another target creature.",
                List.of(
                        TargetFilters.creatureYouControl(),
                        TargetFilters.creature()
                ),
                2,
                2
        ));
    }
}
