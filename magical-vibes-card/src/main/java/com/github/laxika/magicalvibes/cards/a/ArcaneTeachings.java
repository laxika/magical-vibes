package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityToEnchantedCreatureEffect;

import java.util.List;

public class ArcaneTeachings extends Card {

    public ArcaneTeachings() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new BoostEnchantedCreatureEffect(2, 2));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityToEnchantedCreatureEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new DealDamageToAnyTargetEffect(1)),
                        true,
                        "{T}: This creature deals 1 damage to any target."
                )
        ));
    }
}
