package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseTwoPlayersOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageBetweenChosenPlayersEffect;

@CardRegistration(set = "C14", collectorNumber = "32")
@CardRegistration(set = "CM2", collectorNumber = "84")
public class BitterFeud extends Card {

    public BitterFeud() {
        addStaticEffect(new ChooseTwoPlayersOnEnterEffect());
        addStaticEffect(new DoubleDamageBetweenChosenPlayersEffect());
    }
}
