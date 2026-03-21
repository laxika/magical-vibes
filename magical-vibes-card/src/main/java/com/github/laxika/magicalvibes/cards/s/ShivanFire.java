package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.KickerReplacementEffect;

@CardRegistration(set = "DOM", collectorNumber = "142")
public class ShivanFire extends Card {

    public ShivanFire() {
        // Kicker {4}
        addEffect(EffectSlot.STATIC, new KickerEffect("{4}"));
        // Shivan Fire deals 2 damage to target creature.
        // If this spell was kicked, it deals 4 damage instead.
        addEffect(EffectSlot.SPELL, new KickerReplacementEffect(
                new DealDamageToTargetCreatureEffect(2),
                new DealDamageToTargetCreatureEffect(4)));
    }
}
