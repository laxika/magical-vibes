package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CastDuringMainPhase;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "RNA", collectorNumber = "209")
public class SphinxsInsight extends Card {

    public SphinxsInsight() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new ConditionalEffect(new CastDuringMainPhase(), new GainLifeEffect(2)));
    }
}
