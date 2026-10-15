package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;

@CardRegistration(set = "WHO", collectorNumber = "80")
@CardRegistration(set = "WHO", collectorNumber = "685")
public class DecayingTimeLoop extends Card {

    public DecayingTimeLoop() {
        addEffect(EffectSlot.SPELL, new DiscardOwnHandThenDrawThatManyEffect());
        addCastingOption(new Retrace());
    }
}
