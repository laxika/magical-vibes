package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "306")
@CardRegistration(set = "TD2", collectorNumber = "71")
@CardRegistration(set = "UMA", collectorNumber = "232")
@CardRegistration(set = "SLC", collectorNumber = "16")
@CardRegistration(set = "SLC", collectorNumber = "43")
@CardRegistration(set = "2X2", collectorNumber = "311")
public class PhyrexianAltar extends Card {

    public PhyrexianAltar() {
        // Sacrifice a creature: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeCreatureCost(), new AwardAnyColorManaEffect()),
                "Sacrifice a creature: Add one mana of any color."
        ));
    }
}
