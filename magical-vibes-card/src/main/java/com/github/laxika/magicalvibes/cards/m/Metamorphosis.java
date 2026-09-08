package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "CHR", collectorNumber = "66")
public class Metamorphosis extends Card {

    public Metamorphosis() {
        // As an additional cost to cast this spell, sacrifice a creature; its mana value is used below.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(true));
        // Add one plus the sacrificed creature's mana value as any one color, restricted to creature spells.
        addEffect(EffectSlot.SPELL, new AwardAnyColorManaEffect(
                new Sum(new Fixed(1), new XValue()),
                ManaSpendRestriction.CREATURE_SPELL_ONLY,
                null));
    }
}
