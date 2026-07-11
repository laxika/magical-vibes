package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "86")
public class CruelBargain extends Card {

    public CruelBargain() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(4));
        // "Lose half your life, rounded up" — ceil(life/2) == floor((life+1)/2).
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(
                new Divided(new Sum(new ControllerLifeTotal(), new Fixed(1)), 2),
                LoseLifeRecipient.CONTROLLER));
    }
}
