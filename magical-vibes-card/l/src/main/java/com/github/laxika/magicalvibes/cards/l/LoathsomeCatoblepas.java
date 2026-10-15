package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "95")
public class LoathsomeCatoblepas extends Card {

    public LoathsomeCatoblepas() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_DEATH, new BoostTargetCreatureEffect(-3, -3));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BE_BLOCKED, GrantScope.SELF)),
                "{2}{G}: This creature must be blocked this turn if able."
        ));
    }
}
