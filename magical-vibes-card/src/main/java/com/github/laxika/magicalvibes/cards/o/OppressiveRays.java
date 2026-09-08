package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatTaxKind;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCombatTaxEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M15", collectorNumber = "21")
@CardRegistration(set = "JOU", collectorNumber = "19")
public class OppressiveRays extends Card {

    public OppressiveRays() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCombatTaxEffect(3, CombatTaxKind.ATTACK))
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCombatTaxEffect(3, CombatTaxKind.BLOCK_WITH))
                .addEffect(EffectSlot.STATIC, new IncreaseActivatedAbilityCostEffect(
                        new PermanentIsHostOfSourceAuraPredicate(), 3));
    }
}
