package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "45")
@CardRegistration(set = "PIP", collectorNumber = "573")
@CardRegistration(set = "PIP", collectorNumber = "382")
@CardRegistration(set = "PIP", collectorNumber = "910")
public class HancockGhoulishMayor extends Card {

    public HancockGhoulishMayor() {
        CountersOnSource counters = new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                counters,
                counters,
                GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ZOMBIE, CardSubtype.MUTANT))));
    }
}
