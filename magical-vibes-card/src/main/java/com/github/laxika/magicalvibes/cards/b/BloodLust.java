package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Min;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "5ED", collectorNumber = "212")
public class BloodLust extends Card {

    public BloodLust() {
        // If target creature has toughness 5 or greater, it gets +4/-4 until end of turn.
        // Otherwise, it gets +4/-X until end of turn, where X is its toughness minus 1.
        //
        // Both branches reduce toughness by min(4, toughness - 1): a 5+ toughness creature loses
        // exactly 4, anything smaller drops to a toughness of 1. So the power boost is a flat +4
        // and the toughness change is -min(4, toughness - 1).
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                new Fixed(4),
                new Scaled(new Min(new Fixed(4), new Sum(new TargetToughness(), new Fixed(-1))), -1)));
    }
}
