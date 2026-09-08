package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentPower;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentToughness;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "ROE", collectorNumber = "197")
public class MomentousFall extends Card {

    public MomentousFall() {
        // As an additional cost to cast this spell, sacrifice a creature.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(false, true, true));
        // You draw cards equal to the sacrificed creature's power, then you gain life equal to its toughness.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new SacrificedPermanentPower()));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new SacrificedPermanentToughness()));
    }
}
