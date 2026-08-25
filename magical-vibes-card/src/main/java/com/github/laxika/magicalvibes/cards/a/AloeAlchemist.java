package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "152")
public class AloeAlchemist extends Card {

    public AloeAlchemist() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{G}"))));
        addEffect(EffectSlot.ON_SELF_BECOMES_PLOTTED, new BoostTargetCreatureEffect(3, 2));
        addEffect(EffectSlot.ON_SELF_BECOMES_PLOTTED,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }
}
