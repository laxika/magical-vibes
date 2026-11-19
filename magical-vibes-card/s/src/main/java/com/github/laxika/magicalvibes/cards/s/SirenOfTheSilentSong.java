package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "BNG", collectorNumber = "155")
public class SirenOfTheSilentSong extends Card {

    public SirenOfTheSilentSong() {
        addTrigger(Triggers.becomesUntapped(),
                new DiscardEffect(1, Subjects.eachOpponent()),
                new MillEffect(1, Subjects.eachOpponent()));
    }
}
