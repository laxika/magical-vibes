package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "71")
public class GraveStrength extends Card {

    public GraveStrength() {
        // Mill three cards, then put a +1/+1 counter on target creature for each creature card in
        // your graveyard.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new MillEffect(3, MillRecipient.CONTROLLER),
                new PutCounterOnTargetPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER))));
    }
}
