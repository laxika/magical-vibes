package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoomsdayEffect;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "6ED", collectorNumber = "121")
@CardRegistration(set = "WTH", collectorNumber = "66")
@CardRegistration(set = "MP2", collectorNumber = "42")
public class Doomsday extends Card {

    public Doomsday() {
        addEffect(EffectSlot.SPELL, new DoomsdayEffect());
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(
                new Divided(new Sum(new ControllerLifeTotal(), new Fixed(1)), 2),
                LoseLifeRecipient.CONTROLLER));
    }
}
