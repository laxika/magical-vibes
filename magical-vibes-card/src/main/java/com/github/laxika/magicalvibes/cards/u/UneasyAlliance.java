package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "28")
public class UneasyAlliance extends Card {

    public UneasyAlliance() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(
                        new SacrificeSelfCost(),
                        new ExileEnchantedCreatureEffect(),
                        new CreateTokenEffect("Ninja", 1, 1, CardColor.BLACK,
                                List.of(CardSubtype.NINJA), Set.of(), Set.of())
                ),
                "{5}, Sacrifice this Aura: Exile enchanted creature. You create a 1/1 black Ninja creature token. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
