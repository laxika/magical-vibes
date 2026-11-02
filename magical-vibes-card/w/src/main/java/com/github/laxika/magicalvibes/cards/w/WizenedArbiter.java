package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ExchangeCardFromOutsideGameWithHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB1", collectorNumber = "14")
public class WizenedArbiter extends Card {

    public WizenedArbiter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExchangeCardFromOutsideGameWithHandEffect(new CardColorPredicate(CardColor.WHITE)));
    }
}
