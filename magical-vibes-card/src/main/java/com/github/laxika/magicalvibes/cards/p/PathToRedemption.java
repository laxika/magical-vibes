package com.github.laxika.magicalvibes.cards.p;

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

@CardRegistration(set = "TLA", collectorNumber = "31")
public class PathToRedemption extends Card {

    public PathToRedemption() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(
                        new SacrificeSelfCost(),
                        new ExileEnchantedCreatureEffect(),
                        new CreateTokenEffect(
                                "Ally", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.ALLY), Set.of(), Set.of()
                        )
                ),
                "{5}, Sacrifice this Aura: Exile enchanted creature. Create a 1/1 white Ally creature token. Activate only during your turn.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
