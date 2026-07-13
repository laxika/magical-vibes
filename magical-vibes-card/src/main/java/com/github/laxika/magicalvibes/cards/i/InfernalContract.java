package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "7ED", collectorNumber = "143")
public class InfernalContract extends Card {

    public InfernalContract() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(4));
        // "You lose half your life, rounded up." ceil(n/2) == floor((n + 1) / 2).
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(
                new Divided(new Sum(new ControllerLifeTotal(), new Fixed(1)), 2),
                LoseLifeRecipient.CONTROLLER));
    }
}
