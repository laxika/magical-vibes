package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "M15", collectorNumber = "40")
public class TriplicateSpirits extends Card {

    public TriplicateSpirits() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.whiteSpirit(3));
    }
}
