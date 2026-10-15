package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AbandonSchemeUnlessDiscardOrPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "333")
public class FearMyAuthority extends Card {

    public FearMyAuthority() {
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(2, 2, Set.of(Keyword.FEAR), GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new AbandonSchemeUnlessDiscardOrPayLifeEffect(3));
    }
}
