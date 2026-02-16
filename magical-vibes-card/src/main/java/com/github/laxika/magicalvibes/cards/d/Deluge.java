package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.WithoutKeywordTargetFilter;

import java.util.Set;

public class Deluge extends Card {

    public Deluge() {
        addEffect(EffectSlot.SPELL, new TapCreaturesEffect(Set.of(new WithoutKeywordTargetFilter(Keyword.FLYING))));
    }
}
