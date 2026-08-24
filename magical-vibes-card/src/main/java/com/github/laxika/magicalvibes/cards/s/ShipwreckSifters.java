package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasDisturbPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "74")
public class ShipwreckSifters extends Card {

    public ShipwreckSifters() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                SequenceEffect.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)));

        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new TriggeringCardConditionalEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardHasDisturbPredicate())),
                new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
