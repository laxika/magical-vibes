package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealOpponentHandsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "113")
@CardRegistration(set = "M10", collectorNumber = "74")
@CardRegistration(set = "9ED", collectorNumber = "101")
@CardRegistration(set = "8ED", collectorNumber = "105")
@CardRegistration(set = "7ED", collectorNumber = "102")
public class Telepathy extends Card {

    public Telepathy() {
        addEffect(EffectSlot.STATIC, new RevealOpponentHandsEffect());
    }
}
