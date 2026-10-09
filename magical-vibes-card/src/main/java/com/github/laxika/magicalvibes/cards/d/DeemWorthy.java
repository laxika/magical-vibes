package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "127")
@CardRegistration(set = "AKR", collectorNumber = "150")
public class DeemWorthy extends Card {

    public DeemWorthy() {
        // Deem Worthy deals 7 damage to target creature. (DealDamageToTargetCreatureEffect's
        // TargetSpec narrows the legal target to a creature — no explicit filter needed.)
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(7));

        addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(
                new DealDamageToTargetCreatureEffect(2), "Have Deem Worthy deal 2 damage to target creature?"));
        addCycling("{3}{R}");
    }
}
