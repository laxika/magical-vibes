package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "9ED", collectorNumber = "162")
public class SerpentWarrior extends Card {

    public SerpentWarrior() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(3));
    }
}
