package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "69")
public class ParanormalAnalyst extends Card {

    public ParanormalAnalyst() {
        addTrigger(Triggers.youManifestDread(), new PutCardsIntoHandEffect(Subjects.triggeringCards()));
    }
}
