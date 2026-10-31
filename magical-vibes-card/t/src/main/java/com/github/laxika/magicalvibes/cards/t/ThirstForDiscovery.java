package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "VOW", collectorNumber = "85")
@CardRegistration(set = "DBL", collectorNumber = "352")
public class ThirstForDiscovery extends Card {

    public ThirstForDiscovery() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new DiscardEffect(2, DiscardRecipient.CONTROLLER, CardPredicateUtils.basicLand()));
    }
}
