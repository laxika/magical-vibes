package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ImprintedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.AdjustTimeCountersOnEachSuspendedCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandCost;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersOnImprintedCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "OPC2", collectorNumber = "36")
public class TalonGates extends Card {

    public TalonGates() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExileCardFromHandCost(
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                                "nonland"),
                        new PutTimeCountersOnImprintedCardEffect(new ImprintedCardManaValue())
                ),
                "Exile a nonland card from your hand with X time counters on it, where X is its mana value. "
                        + "If the exiled card doesn't have suspend, it gains suspend.",
                ActivationTimingRestriction.SORCERY_SPEED));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new AdjustTimeCountersOnEachSuspendedCardEffect(false, true));
    }
}
