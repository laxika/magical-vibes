package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "7")
public class AvenBrigadier extends Card {

    public AvenBrigadier() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.BIRD))));
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SOLDIER))));
    }
}
