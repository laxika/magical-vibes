package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DMU", collectorNumber = "71")
public class TolarianGeyser extends Card {

    public TolarianGeyser() {
        addStaticEffect(new KickerEffect("{W}"));
        target(TargetFilters.creature()).addSpellEffect(ReturnToHandEffect.target());
        addSpellEffect(new DrawCardEffect());
        addSpellEffect(new ConditionalEffect(new Kicked(), new GainLifeEffect(3)));
    }
}
