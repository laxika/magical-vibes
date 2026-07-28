package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AddNotedManaForLastExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.NoteManaSpentForActivationEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "321")
public class IceCauldron extends Card {

    public IceCauldron() {
        // {X}, {T}: You may exile a nonland card from your hand. You may cast that card for as long
        // as it remains exiled. Put a charge counter on Ice Cauldron and note the type and amount of
        // mana spent to pay this activation cost. Activate only if there are no charge counters on
        // Ice Cauldron.
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(
                        new MayEffect(new ExileFromHandToImprintEffect(
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)), "a nonland card", true),
                                "You may exile a nonland card from your hand."),
                        new PutCountersOnSelfEffect(CounterType.CHARGE),
                        new NoteManaSpentForActivationEffect()
                ),
                "{X}, {T}: You may exile a nonland card from your hand. You may cast that card for as long as it remains exiled. Put a charge counter on Ice Cauldron and note the type and amount of mana spent to pay this activation cost. Activate only if there are no charge counters on Ice Cauldron.")
                .withActivationCondition(new NotCondition(new SourceCounterThreshold(1, CounterType.CHARGE)),
                        "no charge counters on Ice Cauldron"));

        // {T}, Remove a charge counter from Ice Cauldron: Add this artifact's last noted type and
        // amount of mana. Spend this mana only to cast the last card exiled with Ice Cauldron.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(
                        new RemoveChargeCountersFromSourceCost(1),
                        new AddNotedManaForLastExiledCardEffect()
                ),
                "{T}, Remove a charge counter from Ice Cauldron: Add this artifact's last noted type and amount of mana. Spend this mana only to cast the last card exiled with Ice Cauldron."));
    }
}
