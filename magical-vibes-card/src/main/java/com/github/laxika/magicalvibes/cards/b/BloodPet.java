package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "121")
@CardRegistration(set = "6ED", collectorNumber = "115")
public class BloodPet extends Card {

    public BloodPet() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new AwardManaEffect(ManaColor.BLACK)
                ),
                "Sacrifice this creature: Add {B}."
        ));
    }
}
