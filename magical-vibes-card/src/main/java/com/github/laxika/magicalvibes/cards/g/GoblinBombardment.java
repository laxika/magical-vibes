package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "179")
@CardRegistration(set = "TPR", collectorNumber = "136")
@CardRegistration(set = "DDN", collectorNumber = "24")
@CardRegistration(set = "WOT", collectorNumber = "43")
@CardRegistration(set = "MAR", collectorNumber = "23")
@CardRegistration(set = "OMB", collectorNumber = "23")
@CardRegistration(set = "C13", collectorNumber = "110")
public class GoblinBombardment extends Card {

    public GoblinBombardment() {
        // Sacrifice a creature: This enchantment deals 1 damage to any target. The sacrifice is
        // written before the colon, so it is a cost paid at activation, not an effect.
        addActivatedAbility(new ActivatedAbility(
                false,
                "",
                List.of(
                        new SacrificeCreatureCost(),
                        new DealDamageToAnyTargetEffect(1)
                ),
                "Sacrifice a creature: This enchantment deals 1 damage to any target."
        ));
    }
}
