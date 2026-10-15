package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "M12", collectorNumber = "10")
public class BenalishVeteran extends Card {

    public BenalishVeteran() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(1, 1));
    }
}
