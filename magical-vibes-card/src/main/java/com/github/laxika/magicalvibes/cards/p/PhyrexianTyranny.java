package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;

@CardRegistration(set = "PLS", collectorNumber = "118")
public class PhyrexianTyranny extends Card {

    public PhyrexianTyranny() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new LoseLifeUnlessPaysEffect(2, 2));
        addEffect(EffectSlot.ON_OPPONENT_DRAWS, new LoseLifeUnlessPaysEffect(2, 2));
    }
}
