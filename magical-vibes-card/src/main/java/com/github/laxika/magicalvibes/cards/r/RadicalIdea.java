package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.JumpStartCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "GRN", collectorNumber = "52")
public class RadicalIdea extends Card {

    public RadicalIdea() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addCastingOption(new JumpStartCast());
    }
}
