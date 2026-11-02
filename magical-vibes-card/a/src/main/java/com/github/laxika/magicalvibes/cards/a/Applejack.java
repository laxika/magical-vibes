package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ApplejackFamilyGatheringEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1537")
public class Applejack extends Card {

    public Applejack() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ApplejackFamilyGatheringEffect());
    }
}
