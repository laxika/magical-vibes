package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "52")
public class FieryBombardment extends Card {

    public FieryBombardment() {
        // Chroma — {2}, Sacrifice a creature: This enchantment deals damage to any target equal to
        // the number of red mana symbols in the sacrificed creature's mana cost. The sacrifice cost
        // snapshots that symbol count into the entry's xValue.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeCreatureCost(ManaColor.RED),
                        new DealDamageToAnyTargetEffect(new XValue())
                ),
                "Chroma — {2}, Sacrifice a creature: This enchantment deals damage to any target "
                        + "equal to the number of red mana symbols in the sacrificed creature's mana cost."
        ));
    }
}
