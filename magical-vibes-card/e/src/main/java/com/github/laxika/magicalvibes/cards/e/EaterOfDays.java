package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DST", collectorNumber = "120")
@CardRegistration(set = "MB1", collectorNumber = "120")
public class EaterOfDays extends Card {

    public EaterOfDays() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new SkipNextEffect(SkipKind.TURN),
                new SkipNextEffect(SkipKind.TURN)));
    }
}
