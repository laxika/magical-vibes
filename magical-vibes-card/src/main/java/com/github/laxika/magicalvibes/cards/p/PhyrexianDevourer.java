package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ImprintedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "125")
public class PhyrexianDevourer extends Card {

    public PhyrexianDevourer() {
        // "When this creature's power is 7 or greater, sacrifice it." — state-triggered ability;
        // the layer-aware source predicate reads effective power, so the +1/+1 counters count.
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                new PermanentPowerAtLeastPredicate(7),
                List.of(new SacrificeSelfEffect()),
                "Phyrexian Devourer's state-triggered ability"
        ));

        // "Exile the top card of your library: Put X +1/+1 counters on this creature, where X is
        // the exiled card's mana value." The exile cost imprints what it exiled so the amount can
        // read it back at resolution.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(
                        new ExileTopCardOfLibraryCost(1, true),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new ImprintedCardManaValue())
                ),
                "Exile the top card of your library: Put X +1/+1 counters on this creature, "
                        + "where X is the exiled card's mana value."
        ));
    }
}
