package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "7ED", collectorNumber = "162")
@CardRegistration(set = "8ED", collectorNumber = "161")
@CardRegistration(set = "9ED", collectorNumber = "162")
@CardRegistration(set = "POR", collectorNumber = "109")
public class SerpentWarrior extends Card {

    public SerpentWarrior() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(3));
    }
}
