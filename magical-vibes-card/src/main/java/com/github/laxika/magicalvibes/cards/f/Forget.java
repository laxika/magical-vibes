package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "70")
@CardRegistration(set = "5ED", collectorNumber = "89")
public class Forget extends Card {

    public Forget() {
        addEffect(EffectSlot.SPELL, new TargetPlayerDiscardsThenDrawsThatManyEffect(2));
    }
}
