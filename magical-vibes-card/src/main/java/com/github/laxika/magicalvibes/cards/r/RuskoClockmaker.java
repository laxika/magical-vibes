package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "263")
@CardRegistration(set = "YBRO", collectorNumber = "24")
public class RuskoClockmaker extends Card {

    public RuskoClockmaker() {
        // When Rusko enters the battlefield, conjure a card named Midnight Clock onto the battlefield.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConjureCardToBattlefieldEffect("Midnight Clock"));

        // Whenever you cast a noncreature spell, put an hour counter on each Midnight Clock you control.
        // Each opponent loses 1 life and you gain 1 life.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(
                                new PutCounterOnEachControlledPermanentEffect(
                                        CounterType.HOUR, 1,
                                        new PermanentNamedPredicate("Midnight Clock")),
                                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                                new GainLifeEffect(1))));
    }
}
