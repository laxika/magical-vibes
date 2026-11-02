package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "166")
@CardRegistration(set = "TMT", collectorNumber = "249")
public class RaphLeoSiblingRivals extends Card {

    public RaphLeoSiblingRivals() {
        target(TargetFilters.attackingCreature(), 1, 2)
                .addTrigger(Triggers.attacks(), new ConditionalEffect(new FirstCombatPhase(),
                        new UntapPermanentsEffect(TapUntapScope.TARGET)));
        addTrigger(Triggers.attacks(), new ConditionalEffect(new FirstCombatPhase(),
                new AdditionalCombatPhaseEffect(1)));
    }
}
