package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "155")
public class SirenOfTheSilentSong extends Card {

    public SirenOfTheSilentSong() {
        addTrigger(Triggers.becomesUntapped(),
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
        addTrigger(Triggers.becomesUntapped(),
                new MillEffect(1, MillRecipient.EACH_OPPONENT));
    }
}
