package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMC", collectorNumber = "22")
@CardRegistration(set = "TMC", collectorNumber = "90")
public class CaseyJonesBackAlleyBrute extends Card {

    public CaseyJonesBackAlleyBrute() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.ON_ATTACK,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addEffect(EffectSlot.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE,
                        new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER));
    }
}
