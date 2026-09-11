package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "185")
public class GenerousVisitor extends Card {

    public GenerousVisitor() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardTypePredicate(CardType.ENCHANTMENT),
                        List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                        null,
                        TargetFilters.creature()));
    }
}
