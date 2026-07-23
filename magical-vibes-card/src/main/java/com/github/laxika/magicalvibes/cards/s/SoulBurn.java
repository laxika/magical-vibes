package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.SoulBurnEffect;

@CardRegistration(set = "ICE", collectorNumber = "161")
public class SoulBurn extends Card {

    public SoulBurn() {
        // Spend only black and/or red mana on X.
        setXColorRestrictions(ManaColor.BLACK, ManaColor.RED);

        // Deals X damage to any target; gain life equal to damage dealt, capped by {B} spent on X
        // and the target's life/loyalty/toughness before the damage.
        addEffect(EffectSlot.SPELL, new SoulBurnEffect());
    }
}
