package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "156")
public class FearsomeGoblinPair extends Card {

    public FearsomeGoblinPair() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new AmassGoblinsEffect(4));
    }
}
