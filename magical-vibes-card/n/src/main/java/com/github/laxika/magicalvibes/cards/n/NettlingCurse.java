package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "48")
public class NettlingCurse extends Card {

    public NettlingCurse() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.attacks(), new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER))
                .addTrigger(Triggers.blocks(),
                        TriggerOptions.ONE_OR_MORE,
                        new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new SetCombatRequirementThisTurnEffect(
                        CombatRequirement.MUST_ATTACK, GrantScope.ENCHANTED_CREATURE)),
                "{1}{R}: Enchanted creature attacks this turn if able."
        ));
    }
}
