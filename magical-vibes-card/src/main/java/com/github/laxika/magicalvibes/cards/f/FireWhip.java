package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "100")
public class FireWhip extends Card {

    public FireWhip() {
        // Enchant creature you control — grants it "{T}: This creature deals 1 damage to any target."
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                null,
                                List.of(new DealDamageToAnyTargetEffect(1)),
                                "{T}: This creature deals 1 damage to any target."
                        ),
                        GrantScope.ENCHANTED_CREATURE
                ));

        // Sacrifice this Aura: It deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(1)),
                "Sacrifice Fire Whip: Fire Whip deals 1 damage to any target."
        ));
    }
}
