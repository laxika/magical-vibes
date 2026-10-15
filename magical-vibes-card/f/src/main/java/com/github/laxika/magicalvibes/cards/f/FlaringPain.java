package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;

@CardRegistration(set = "JUD", collectorNumber = "89")
public class FlaringPain extends Card {

    public FlaringPain() {
        addEffect(EffectSlot.SPELL, new DamageCantBePreventedThisTurnEffect());
        addCastingOption(new FlashbackCast("{R}"));
    }
}
