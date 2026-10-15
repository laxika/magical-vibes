package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

/** Bloodletting, the prepare spell of Leech Collector // Bloodletting. */
public class Bloodletting extends Card {

    public Bloodletting() {
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));
    }
}
