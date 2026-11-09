package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerMainPhase;
import com.github.laxika.magicalvibes.model.condition.ExactlyAttackers;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "69")
public class ValorsReach extends Card {

    public ValorsReach() {
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withEventCondition(new ExactlyAttackers(2)),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.DECLARED_ATTACKERS));
        target(TargetFilters.creatureYouControl(), 0, 2)
                .addTrigger(Triggers.chaosEnsues(),
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS));
        addTrigger(Triggers.chaosEnsues(),
                new ConditionalEffect(
                        new ControllerMainPhase(),
                        new AdditionalCombatMainPhaseEffect(1)));
    }
}
