package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "148")
public class GhirapurAetherGrid extends Card {

    public GhirapurAetherGrid() {
        // Tapping the two artifacts is written before the colon, so it is a cost paid at
        // activation. The grid itself is an enchantment, so it never taps for its own cost.
        addActivatedAbility(new ActivatedAbility(
                false,
                "",
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsArtifactPredicate()),
                        new DealDamageToAnyTargetEffect(1)
                ),
                "Tap two untapped artifacts you control: This enchantment deals 1 damage to any target."
        ));
    }
}
