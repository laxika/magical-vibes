package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "306")
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
