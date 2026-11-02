package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "30")
@CardRegistration(set = "OM1", collectorNumber = "35")
@CardRegistration(set = "MSC", collectorNumber = "784")
public class DocOcksHenchmen extends Card {

    public DocOcksHenchmen() {
        addTrigger(Triggers.attacks(), new DrawDiscardAndConniveEffect());
    }
}
