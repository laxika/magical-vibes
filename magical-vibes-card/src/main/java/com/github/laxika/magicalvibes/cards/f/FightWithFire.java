package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageAmongAnyTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.KickerReplacementEffect;

@CardRegistration(set = "DOM", collectorNumber = "119")
public class FightWithFire extends Card {

    public FightWithFire() {
        // Kicker {5}{R}
        addEffect(EffectSlot.STATIC, new KickerEffect("{5}{R}"));
        // Fight with Fire deals 5 damage to target creature.
        // If this spell was kicked, it deals 10 damage divided as you choose
        // among any number of targets instead.
        addEffect(EffectSlot.SPELL, new KickerReplacementEffect(
                new DealDamageToTargetCreatureEffect(5),
                new DealDividedDamageAmongAnyTargetsEffect(10)));
    }
}
