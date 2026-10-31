package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromMulticoloredEffect;

@CardRegistration(set = "DIS", collectorNumber = "44")
public class EnemyOfTheGuildpact extends Card {

    public EnemyOfTheGuildpact() {
        addStaticEffect(new ProtectionFromMulticoloredEffect());
    }
}
