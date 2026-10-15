package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromEnemyColoredMulticoloredEffect;

@CardRegistration(set = "MB1", collectorNumber = "74")
public class FrenemyOfTheGuildpact extends Card {

    public FrenemyOfTheGuildpact() {
        addEffect(EffectSlot.STATIC, new ProtectionFromEnemyColoredMulticoloredEffect());
    }
}
