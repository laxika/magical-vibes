package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "INR", collectorNumber = "75")
public class MemoryDeluge extends Card {

    public MemoryDeluge() {
        // Look at the top X cards of your library, where X is the amount of mana spent to cast this
        // spell. Put two of them into your hand and the rest on the bottom of your library in a
        // random order.
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(
                new ManaSpentToCast(), 2));
        // Flashback {5}{U}{U}
        addCastingOption(new FlashbackCast("{5}{U}{U}"));
    }
}
