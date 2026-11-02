package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardThenIfMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "143")
public class EntrailsFeaster extends Card {

    public EntrailsFeaster() {
        CardTypePredicate creatureCard = new CardTypePredicate(CardType.CREATURE);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ExileAnyGraveyardCardThenIfMatchesEffect(
                        creatureCard,
                        creatureCard,
                        new PutCountersOnSourceEffect(1, 1, 1),
                        new TapPermanentsEffect(TapUntapScope.SELF)));
    }
}
