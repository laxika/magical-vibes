package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HBG", collectorNumber = "199")
@CardRegistration(set = "SLD", collectorNumber = "2487")
public class AncientBronzeDragon extends Card {

    public AncientBronzeDragon() {
        PutCounterOnTargetPermanentEffect putCounters = new PutCounterOnTargetPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new EventValue());
        QueueReflexiveAbilityEffect putCountersAfterRoll =
                new QueueReflexiveAbilityEffect(putCounters, false, true);

        // Whenever this creature deals combat damage to a player, roll a d20. When you do, put X
        // +1/+1 counters on each of up to two target creatures, where X is the result.
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new RollD20Effect(putCountersAfterRoll, putCountersAfterRoll));
    }
}
