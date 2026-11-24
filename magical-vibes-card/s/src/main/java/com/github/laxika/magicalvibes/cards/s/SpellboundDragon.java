package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "90")
@CardRegistration(set = "C17", collectorNumber = "196")
public class SpellboundDragon extends Card {

    public SpellboundDragon() {
        // Flying is loaded from Scryfall metadata.
        // Whenever this creature attacks, draw a card, then discard a card. This creature gets
        // +X/+0 until end of turn, where X is the discarded card's mana value.
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                new DrawCardEffect(1),
                new DiscardEffect(1, Subjects.you()),
                new BoostSelfEffect(new LastDiscardedCardManaValue(), new Fixed(0))));
    }
}
