package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsDuringCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VMA", collectorNumber = "244")
public class BasandraBattleSeraph extends Card {

    public BasandraBattleSeraph() {
        // Players can't cast spells during combat.
        addEffect(EffectSlot.STATIC, new PlayersCantCastSpellsDuringCombatEffect());

        // {R}: Target creature attacks this turn if able.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK)),
                "{R}: Target creature attacks this turn if able.",
                TargetFilters.creature()
        ));
    }
}
