package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureCantBlockThisTurnEffect;

@CardRegistration(set = "10E", collectorNumber = "240")
public class Stun extends Card {

    public Stun() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new TargetCreatureCantBlockThisTurnEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
