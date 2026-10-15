package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "157")
public class CaterwaulingBoggart extends Card {

    public CaterwaulingBoggart() {
        // Goblins you control and Elementals you control have menace (including self).
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 0, Set.of(Keyword.MENACE),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN, CardSubtype.ELEMENTAL))));
    }
}
