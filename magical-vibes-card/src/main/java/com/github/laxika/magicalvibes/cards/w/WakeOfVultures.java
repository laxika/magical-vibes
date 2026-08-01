package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "74")
public class WakeOfVultures extends Card {

    public WakeOfVultures() {
        // {1}{B}, Sacrifice a creature: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new SacrificeCreatureCost(), new RegenerateEffect()),
                "{1}{B}, Sacrifice a creature: Regenerate this creature."
        ));
    }
}
