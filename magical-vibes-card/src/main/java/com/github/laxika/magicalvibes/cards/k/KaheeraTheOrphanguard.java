package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "224")
public class KaheeraTheOrphanguard extends Card {

    public KaheeraTheOrphanguard() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, Set.of(Keyword.VIGILANCE), GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.CAT,
                        CardSubtype.ELEMENTAL,
                        CardSubtype.NIGHTMARE,
                        CardSubtype.DINOSAUR,
                        CardSubtype.BEAST
                ))));
    }
}
