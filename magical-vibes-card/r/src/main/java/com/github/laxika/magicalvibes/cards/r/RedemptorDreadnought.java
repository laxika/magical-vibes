package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.condition.SourceExiledCardsThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "164")
public class RedemptorDreadnought extends Card {

    public RedemptorDreadnought() {
        addCastingCost(new ExileAnyNumberOfCardsFromGraveyardCost(
                new CardTypePredicate(CardType.CREATURE), 0, 1));
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceExiledCardsThreshold(1)),
                new BoostSelfEffect(new TotalPowerOfCardsExiledWithSource(), new TotalPowerOfCardsExiledWithSource()));
    }
}
