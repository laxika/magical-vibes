package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "96")
public class DemonlordOfAshmouth extends Card {

    public DemonlordOfAshmouth() {
        // Flying and Undying are auto-loaded from Scryfall.

        // When this creature enters, exile it unless you sacrifice another creature.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ForcedCostOrElseEffect(
                        new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "Sacrifice another creature"),
                        List.of(new ExileSelfEffect()),
                        true));
    }
}
