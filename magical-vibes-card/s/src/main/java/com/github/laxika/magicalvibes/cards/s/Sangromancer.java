package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MBS", collectorNumber = "53")
@CardRegistration(set = "SCD", collectorNumber = "103")
@CardRegistration(set = "C21", collectorNumber = "152")
@CardRegistration(set = "C16", collectorNumber = "116")
@CardRegistration(set = "C17", collectorNumber = "123")
public class Sangromancer extends Card {

    public Sangromancer() {
        // Whenever a creature an opponent controls dies, you may gain 3 life.
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),
                new MayEffect(new GainLifeEffect(3), "Gain 3 life?"));

        // Whenever an opponent discards a card, you may gain 3 life.
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS,
                new MayEffect(new GainLifeEffect(3), "Gain 3 life?"));
    }
}
