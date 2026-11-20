package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "93")
@CardRegistration(set = "C14", collectorNumber = "178")
@CardRegistration(set = "C21", collectorNumber = "173")
@CardRegistration(set = "AFC", collectorNumber = "128")
@CardRegistration(set = "SCD", collectorNumber = "147")
@CardRegistration(set = "CM2", collectorNumber = "104")
public class HoardSmelterDragon extends Card {

    public HoardSmelterDragon() {
        // Destroy target artifact. Hoard-Smelter Dragon gets +X/+0, where X is that artifact's mana value.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(SequenceEffect.of(
                        new DestroyTargetPermanentEffect(),
                        new BoostSelfEffect(new TargetManaValue(), new Fixed(0)))),
                "{3}{R}: Destroy target artifact. Hoard-Smelter Dragon gets +X/+0 until end of turn, where X is that artifact's mana value.",
                TargetFilters.artifact()
        ));
    }
}
