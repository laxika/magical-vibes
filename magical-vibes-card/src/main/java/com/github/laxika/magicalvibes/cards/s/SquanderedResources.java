package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfTypeSacrificedLandCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "137")
public class SquanderedResources extends Card {

    public SquanderedResources() {
        // Sacrifice a land: Add one mana of any type the sacrificed land could produce.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land", false),
                        new AwardManaOfTypeSacrificedLandCouldProduceEffect()),
                "Sacrifice a land: Add one mana of any type the sacrificed land could produce."));
    }
}
