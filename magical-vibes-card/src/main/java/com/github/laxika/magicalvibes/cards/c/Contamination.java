package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandManaWithColorEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "USG", collectorNumber = "123")
public class Contamination extends Card {

    public Contamination() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you sacrifice a creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeUnlessSacrificeOwnPermanentEffect(
                new PermanentIsCreaturePredicate(), "a creature"));

        // If a land is tapped for mana, it produces {B} instead of any other type and amount.
        addEffect(EffectSlot.STATIC, new ReplaceLandManaWithColorEffect(ManaColor.BLACK));
    }
}
