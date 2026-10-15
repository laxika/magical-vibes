package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoGraveyardThenDoubleAndShuffleEffect;

@CardRegistration(set = "YONE", collectorNumber = "25")
public class MephidrossSlime extends Card {

    public MephidrossSlime() {
        addEffect(EffectSlot.ON_DEATH,
                new ConjureCardIntoGraveyardThenDoubleAndShuffleEffect(MephidrossSlime::new));
    }
}
