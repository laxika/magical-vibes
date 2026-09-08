package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "RNA", collectorNumber = "62")
public class BankruptInBlood extends Card {

    public BankruptInBlood() {
        // As an additional cost to cast this spell, sacrifice two creatures.
        addEffect(EffectSlot.SPELL, new SacrificeMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()));
        // Draw three cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
