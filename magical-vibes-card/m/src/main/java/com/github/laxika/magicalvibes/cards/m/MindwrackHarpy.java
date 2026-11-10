package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "276")
public class MindwrackHarpy extends Card {

    public MindwrackHarpy() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new MillEffect(3, MillRecipient.CONTROLLER),
                new MillEffect(3, MillRecipient.EACH_OPPONENT));
    }
}
