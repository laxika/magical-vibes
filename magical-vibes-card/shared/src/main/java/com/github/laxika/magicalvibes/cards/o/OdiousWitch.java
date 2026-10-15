package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

public class OdiousWitch extends Card {

    public OdiousWitch() {
        addEffect(EffectSlot.ON_ATTACK, new LoseLifeEffect(1, LoseLifeRecipient.DEFENDING_PLAYER));
        addEffect(EffectSlot.ON_ATTACK, new GainLifeEffect(1));
    }
}
