package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIC", collectorNumber = "75")
@CardRegistration(set = "FIC", collectorNumber = "126")
public class YunasWhistle extends Card {

    public YunasWhistle() {
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.SPELL, SequenceEffect.of(
                RevealUntilCardPredicateRestOnBottomRandomEffect.toHandRecordingManaValue(
                        new CardTypePredicate(CardType.CREATURE)),
                new PutCounterOnTargetPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, new EventValue())));
    }
}
