package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.condition.SourceExiledCardsThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "40K", collectorNumber = "164")
public class RedemptorDreadnought extends Card {

    public RedemptorDreadnought() {
        addEffect(EffectSlot.SPELL, new ExileAnyNumberOfCardsFromGraveyardCost(
                new CardTypePredicate(CardType.CREATURE), 0, 1));
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new SourceExiledCardsThreshold(1),
                new BoostSelfEffect(new TotalPowerOfCardsExiledWithSource(),
                        new TotalPowerOfCardsExiledWithSource())));
    }
}
