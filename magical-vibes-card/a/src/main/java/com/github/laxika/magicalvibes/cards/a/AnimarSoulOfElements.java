package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "A25", collectorNumber = "196")
@CardRegistration(set = "SLD", collectorNumber = "1891")
@CardRegistration(set = "2X2", collectorNumber = "171")
@CardRegistration(set = "CMD", collectorNumber = "181")
public class AnimarSoulOfElements extends Card {

    public AnimarSoulOfElements() {
        // Protection from white and from black.
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.WHITE, CardColor.BLACK)));

        // Whenever you cast a creature spell, put a +1/+1 counter on Animar.
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new PutCountersOnSourceEffect(1, 1, 1));

        // Creature spells you cast cost {1} less to cast for each +1/+1 counter on Animar.
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardTypePredicate(CardType.CREATURE),
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                CostModificationScope.SELF));
    }
}
