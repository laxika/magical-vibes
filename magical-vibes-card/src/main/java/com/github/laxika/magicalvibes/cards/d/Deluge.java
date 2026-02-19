package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.WithoutKeywordTargetFilter;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "79")
public class Deluge extends Card {

    public Deluge() {
        addEffect(EffectSlot.SPELL, new TapCreaturesEffect(Set.of(new WithoutKeywordTargetFilter(Keyword.FLYING))));
    }
}
