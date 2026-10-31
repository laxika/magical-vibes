package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CastDuringMainPhase;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "RNA", collectorNumber = "45")
@CardRegistration(set = "GN3", collectorNumber = "34")
public class PrecognitivePerception extends Card {

    public PrecognitivePerception() {
        addSpellEffect(new ConditionalEffect(new CastDuringMainPhase(), new ScryEffect(3)));
        addSpellEffect(new DrawCardEffect(3));
    }
}
