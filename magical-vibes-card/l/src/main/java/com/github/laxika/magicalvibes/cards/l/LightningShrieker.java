package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "106")
public class LightningShrieker extends Card {

    public LightningShrieker() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY), ShuffleTargetPermanentIntoLibraryEffect.of(Subjects.source()));
    }
}
