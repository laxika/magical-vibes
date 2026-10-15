package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesDamagedBySourceInsteadOfDyingEffect;

@CardRegistration(set = "CHK", collectorNumber = "177")
public class KumanosPupils extends Card {

    public KumanosPupils() {
        // "If a creature dealt damage by this creature this turn would die, exile it instead."
        addEffect(EffectSlot.STATIC, new ExileCreaturesDamagedBySourceInsteadOfDyingEffect());
    }
}
