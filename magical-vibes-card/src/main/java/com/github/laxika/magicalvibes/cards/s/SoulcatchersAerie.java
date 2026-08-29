package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "JUD", collectorNumber = "25")
public class SoulcatchersAerie extends Card {

    public SoulcatchersAerie() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.BIRD),
                        new PutCountersOnSelfEffect(CounterType.FEATHER)));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1,
                1,
                Set.of(),
                GrantScope.ALL_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.BIRD)),
                CounterType.FEATHER));
    }
}
