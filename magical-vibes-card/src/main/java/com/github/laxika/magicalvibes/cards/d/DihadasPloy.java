package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.JumpStartCast;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedOrCycledThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "MH2", collectorNumber = "193")
public class DihadasPloy extends Card {

    public DihadasPloy() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new CardsDiscardedOrCycledThisTurn()));
        addCastingOption(new JumpStartCast());
    }
}
