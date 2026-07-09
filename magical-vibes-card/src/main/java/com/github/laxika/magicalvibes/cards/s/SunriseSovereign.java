package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "192")
public class SunriseSovereign extends Card {

    public SunriseSovereign() {
        // Other Giant creatures you control get +2/+2 and have trample.
        // (OWN_CREATURES scope excludes the source itself.)
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, Set.of(Keyword.TRAMPLE),
                GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GIANT))));
    }
}
