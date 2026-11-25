package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "THB", collectorNumber = "133")
public class FatefulEnd extends Card {

    public FatefulEnd() {
        addSpellEffect(SequenceEffect.of(
                        DealDamageEffect.toAnyTarget(3),
                        new ScryEffect(1)));
    }
}
