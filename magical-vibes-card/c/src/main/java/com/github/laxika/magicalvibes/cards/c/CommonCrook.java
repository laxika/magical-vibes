package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "53")
@CardRegistration(set = "OM1", collectorNumber = "53")
@CardRegistration(set = "MSC", collectorNumber = "792")
public class CommonCrook extends Card {

    public CommonCrook() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), CreateTokenEffect.ofTreasureToken(1));
    }
}
