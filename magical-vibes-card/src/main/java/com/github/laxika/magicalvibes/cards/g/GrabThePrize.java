package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DSK", collectorNumber = "138")
public class GrabThePrize extends Card {

    public GrabThePrize() {
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null, false, 1, false, false, true));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ImprintedCardMatches(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        "not a land card", "discarded card"),
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT)));
    }
}
