package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "325")
public class KeldonNecropolis extends Card {

    public KeldonNecropolis() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{R}",
                List.of(new SacrificeCreatureCost(), new DealDamageToAnyTargetEffect(2)),
                "{4}{R}, {T}, Sacrifice a creature: Keldon Necropolis deals 2 damage to any target."
        ));
    }
}
