package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "DMU", collectorNumber = "107")
@CardRegistration(set = "DMU", collectorNumber = "290")
public class SheoldredTheApocalypse extends Card {

    public SheoldredTheApocalypse() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new GainLifeEffect(2));
        addEffect(EffectSlot.ON_OPPONENT_DRAWS, new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER));
    }
}
