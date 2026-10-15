package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StaticOrbEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ATQ", collectorNumber = "5")
@CardRegistration(set = "ATQ", collectorNumber = "98")
public class DampingField extends Card {

    public DampingField() {
        addEffect(EffectSlot.STATIC, new StaticOrbEffect(1, new PermanentIsArtifactPredicate(), false));
    }
}
