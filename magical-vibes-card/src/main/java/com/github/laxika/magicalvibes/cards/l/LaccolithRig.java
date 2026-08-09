package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsPowerDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NEM", collectorNumber = "88")
public class LaccolithRig extends Card {

    public LaccolithRig() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                        new MayEffect(
                                SequenceEffect.of(
                                        new EnchantedCreatureDealsPowerDamageToTargetCreatureEffect(),
                                        new AssignNoCombatDamageToEnchantedCreatureEffect()),
                                "Have enchanted creature deal damage equal to its power to target creature?"));
    }
}
