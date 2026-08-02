package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantBlockUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M15", collectorNumber = "21")
public class OppressiveRays extends Card {

    public OppressiveRays() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackUnlessPaysEffect(3))
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantBlockUnlessPaysEffect(3))
                .addEffect(EffectSlot.STATIC, new IncreaseActivatedAbilityCostEffect(
                        new PermanentIsHostOfSourceAuraPredicate(), 3));
    }
}
