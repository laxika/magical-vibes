package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardInsteadOfEmptyLibraryDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "40K", collectorNumber = "46")
public class OutOfTheTombs extends Card {

    public OutOfTheTombs() {
        // "At the beginning of your upkeep, put two eon counters on this enchantment, then mill
        // cards equal to the number of eon counters on it."
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.EON, 2),
                new MillEffect(new CountersOnSource(CounterType.EON), MillRecipient.CONTROLLER)));

        // "If you would draw a card while your library has no cards in it, instead return a creature
        // card from your graveyard to the battlefield. If you can't, you lose the game."
        addEffect(EffectSlot.STATIC, new ReturnCreatureFromGraveyardInsteadOfEmptyLibraryDrawEffect());
    }
}
