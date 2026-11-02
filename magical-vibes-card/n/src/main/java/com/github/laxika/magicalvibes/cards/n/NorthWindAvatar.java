package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "162")
@CardRegistration(set = "TMT", collectorNumber = "248")
public class NorthWindAvatar extends Card {

    public NorthWindAvatar() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new WasCast(true), new SearchOutsideGameToHandEffect()));
    }
}
