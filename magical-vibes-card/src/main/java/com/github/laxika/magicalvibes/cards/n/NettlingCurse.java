package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "48")
public class NettlingCurse extends Card {

    public NettlingCurse() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ATTACK, new EnchantedCreatureControllerLosesLifeEffect(3))
                .addEffect(EffectSlot.ON_BLOCK, new EnchantedCreatureControllerLosesLifeEffect(3));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new SetCombatRequirementThisTurnEffect(
                        CombatRequirement.MUST_ATTACK, GrantScope.ENCHANTED_CREATURE)),
                "{1}{R}: Enchanted creature attacks this turn if able."
        ));
    }
}
