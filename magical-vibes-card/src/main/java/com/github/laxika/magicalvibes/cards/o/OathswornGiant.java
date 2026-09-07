package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "27")
public class OathswornGiant extends Card {

    public OathswornGiant() {
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(0, 2, Set.of(Keyword.VIGILANCE), GrantScope.OWN_CREATURES));
    }
}
