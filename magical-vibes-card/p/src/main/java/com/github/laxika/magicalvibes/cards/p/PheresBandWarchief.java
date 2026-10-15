package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "135")
public class PheresBandWarchief extends Card {

    public PheresBandWarchief() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1,
                Set.of(Keyword.VIGILANCE, Keyword.TRAMPLE), GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.CENTAUR)));
    }
}
