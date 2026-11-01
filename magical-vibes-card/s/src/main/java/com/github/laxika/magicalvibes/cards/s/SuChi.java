package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ATQ", collectorNumber = "66")
@CardRegistration(set = "ME1", collectorNumber = "168")
@CardRegistration(set = "VMA", collectorNumber = "285")
public class SuChi extends Card {

    public SuChi() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new AwardManaEffect(ManaColor.COLORLESS, 4));
    }
}
