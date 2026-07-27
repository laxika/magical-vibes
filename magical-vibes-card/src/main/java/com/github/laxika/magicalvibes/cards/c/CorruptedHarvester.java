package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "59")
public class CorruptedHarvester extends Card {

    public CorruptedHarvester() {
        // {B}, Sacrifice a creature: Regenerate Corrupted Harvester.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new SacrificeCreatureCost(), new RegenerateEffect()),
                "{B}, Sacrifice a creature: Regenerate Corrupted Harvester.",
                TargetFilters.creatureYouControl()
        ));
    }
}
