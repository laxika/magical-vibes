package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardThenIfMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "143")
public class EntrailsFeaster extends Card {

    public EntrailsFeaster() {
        CardTypePredicate creatureCard = new CardTypePredicate(CardType.CREATURE);
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ExileAnyGraveyardCardThenIfMatchesEffect(
                        creatureCard,
                        creatureCard,
                        new PutCountersOnSourceEffect(1, 1, 1),
                        new TapPermanentsEffect(TapUntapScope.SELF)));
    }
}
