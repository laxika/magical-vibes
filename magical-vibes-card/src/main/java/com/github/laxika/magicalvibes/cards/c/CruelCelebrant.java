package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "WAR", collectorNumber = "188")
public class CruelCelebrant extends Card {

    private static final SequenceEffect DEATH_TRIGGER = SequenceEffect.of(
            new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
            new GainLifeEffect(1));

    public CruelCelebrant() {
        addEffect(EffectSlot.ON_DEATH, DEATH_TRIGGER);
        addEffect(EffectSlot.ON_ALLY_CREATURE_OR_PLANESWALKER_DIES, DEATH_TRIGGER);
    }
}
