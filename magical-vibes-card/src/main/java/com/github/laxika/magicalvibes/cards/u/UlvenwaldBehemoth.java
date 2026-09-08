package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

public class UlvenwaldBehemoth extends Card {

    public UlvenwaldBehemoth() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1,
                1,
                Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                GrantScope.OWN_CREATURES
        ));
    }
}
