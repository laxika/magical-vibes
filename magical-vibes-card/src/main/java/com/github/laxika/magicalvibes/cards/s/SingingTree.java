package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ME1", collectorNumber = "130")
public class SingingTree extends Card {

    public SingingTree() {
        // {T}: Target attacking creature has base power 0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(SetBasePowerToughnessEffect.powerOnly(0)),
                "{T}: Target attacking creature has base power 0 until end of turn.",
                TargetFilters.attackingCreature()
        ));
    }
}
