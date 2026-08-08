package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "79")
public class KrasisIncubation extends Card {

    public KrasisIncubation() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantActivateAbilitiesEffect());

        // {1}{G}{U}, Return this Aura to its owner's hand: Put two +1/+1 counters on enchanted
        // creature. The bounce is the cost, so the counters land on the creature the Aura had been
        // enchanting (CR 608.2h last-known information).
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}{U}",
                List.of(new ReturnSelfToHandCost(),
                        new PutCountersOnEnchantedCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "{1}{G}{U}, Return this Aura to its owner's hand: Put two +1/+1 counters on enchanted creature."
        ));
    }
}
