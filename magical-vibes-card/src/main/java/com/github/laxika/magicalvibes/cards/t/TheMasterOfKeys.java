package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GrantEscapeToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DSC", collectorNumber = "4")
public class TheMasterOfKeys extends Card {

    public TheMasterOfKeys() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MillEffect(new Scaled(new XValue(), 2), MillRecipient.CONTROLLER));
        addEffect(EffectSlot.STATIC,
                new GrantEscapeToGraveyardCardsEffect(new CardTypePredicate(CardType.ENCHANTMENT)));
    }
}
