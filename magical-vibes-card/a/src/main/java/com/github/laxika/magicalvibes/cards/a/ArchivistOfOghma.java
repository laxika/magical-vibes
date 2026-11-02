package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "874")
@CardRegistration(set = "HBG", collectorNumber = "83")
public class ArchivistOfOghma extends Card {

    public ArchivistOfOghma() {
        addTrigger(Triggers.opponentSearchesTheirLibrary(), SequenceEffect.of(
                new GainLifeEffect(1),
                new DrawCardEffect()));
    }
}
