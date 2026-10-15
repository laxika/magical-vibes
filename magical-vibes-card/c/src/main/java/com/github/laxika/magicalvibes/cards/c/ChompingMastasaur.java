package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YDFT", collectorNumber = "12")
public class ChompingMastasaur extends Card {

    public ChompingMastasaur() {
        var nonlandCard = new CardNotPredicate(new CardTypePredicate(CardType.LAND));
        var discardThenSeekAndDamage = SequenceEffect.of(
                new DiscardCardThenEffect(null,
                        new DealDamageToAnyTargetEffect(new LastDiscardedCardManaValue()), "a card"),
                new SeekLibraryToHandEffect(nonlandCard));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, discardThenSeekAndDamage);
        addEffect(EffectSlot.ON_ATTACK, discardThenSeekAndDamage);
    }
}
