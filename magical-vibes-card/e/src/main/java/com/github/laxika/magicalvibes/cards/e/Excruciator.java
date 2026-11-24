package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedEffect;

@CardRegistration(set = "RAV", collectorNumber = "121")
public class Excruciator extends Card {

    public Excruciator() {
        addStaticEffect(DamageCantBePreventedEffect.dealtBySource());
    }
}
