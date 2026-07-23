package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "116")
public class BurntOffering extends Card {

    public BurntOffering() {
        // Additional cost: sacrifice a creature; its mana value is snapshotted into xValue.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(true));
        // Add X mana in any combination of {B} and/or {R}.
        addEffect(EffectSlot.SPELL, new AwardManaOfColorsEffect(
                List.of(ManaColor.BLACK, ManaColor.RED), new XValue()));
    }
}
