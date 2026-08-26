package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SourceDamageCantBePreventedEffect;

@CardRegistration(set = "RAV", collectorNumber = "121")
public class Excruciator extends Card {

    public Excruciator() {
        addEffect(EffectSlot.STATIC, new SourceDamageCantBePreventedEffect());
    }
}
