package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseTwoPlayersOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageBetweenChosenPlayersEffect;

@CardRegistration(set = "C14", collectorNumber = "32")
public class BitterFeud extends Card {

    public BitterFeud() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseTwoPlayersOnEnterEffect());
        addEffect(EffectSlot.STATIC, new DoubleDamageBetweenChosenPlayersEffect());
    }
}
