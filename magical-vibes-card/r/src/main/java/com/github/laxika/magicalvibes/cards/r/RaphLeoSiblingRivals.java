package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "166")
@CardRegistration(set = "TMT", collectorNumber = "249")
public class RaphLeoSiblingRivals extends Card {

    public RaphLeoSiblingRivals() {
        var untapPermanents = new UntapPermanentsEffect(TapUntapScope.TARGET);
        target(TargetFilters.attackingCreature(), 1, 2)
                .bindEffect(untapPermanents);
        addTrigger(Triggers.attacks(), TriggerOptions.DEFAULT.withInterveningIf(new FirstCombatPhase()),
                untapPermanents,
                new AdditionalCombatPhaseEffect(1));
    }
}
