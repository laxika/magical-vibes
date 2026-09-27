package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SLD", collectorNumber = "874")
public class ArchivistOfOghma extends Card {

    public ArchivistOfOghma() {
        addEffect(EffectSlot.ON_OPPONENT_SEARCHES_LIBRARY, SequenceEffect.of(
                new GainLifeEffect(1),
                new DrawCardEffect()));
    }
}
