package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "314")
public class CelestialSword extends Card {

    public CelestialSword() {
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(
                        new BoostTargetCreatureEffect(3, 3),
                        new SacrificeTargetPermanentAtEndStepEffect()
                ),
                "{3}, {T}: Target creature you control gets +3/+3 until end of turn. "
                        + "Its controller sacrifices it at the beginning of the next end step.",
                TargetFilters.creatureYouControl()
        ));
    }
}
