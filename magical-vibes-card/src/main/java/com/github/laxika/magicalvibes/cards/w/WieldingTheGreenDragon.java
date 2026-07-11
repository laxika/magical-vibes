package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "PTK", collectorNumber = "157")
public class WieldingTheGreenDragon extends Card {

    public WieldingTheGreenDragon() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, 4));
    }
}
