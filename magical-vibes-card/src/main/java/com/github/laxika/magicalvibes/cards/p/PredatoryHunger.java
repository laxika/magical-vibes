package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "117")
public class PredatoryHunger extends Card {

    public PredatoryHunger() {
        target(TargetFilters.creature())
                // Whenever an opponent casts a creature spell, put a +1/+1 counter on enchanted creature.
                .addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new SpellCastTriggerEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        List.of(new PutCountersOnEnchantedCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                ));
    }
}
