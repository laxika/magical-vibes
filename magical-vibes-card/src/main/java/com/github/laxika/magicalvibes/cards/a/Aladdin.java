package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "44")
public class Aladdin extends Card {

    public Aladdin() {
        // {1}{R}{R}, {T}: Gain control of target artifact for as long as you control this creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}{R}",
                List.of(new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD)),
                "{1}{R}{R}, {T}: Gain control of target artifact for as long as you control this creature.",
                TargetFilters.artifact()));
    }
}
