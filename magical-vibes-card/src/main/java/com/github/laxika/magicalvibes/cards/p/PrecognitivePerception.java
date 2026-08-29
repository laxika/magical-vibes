package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerMainPhase;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "RNA", collectorNumber = "45")
public class PrecognitivePerception extends Card {

    public PrecognitivePerception() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new ControllerMainPhase(), new ScryEffect(3)));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
