package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockedBySourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "191")
@CardRegistration(set = "ATH", collectorNumber = "39")
@CardRegistration(set = "TSB", collectorNumber = "64")
@CardRegistration(set = "SLD", collectorNumber = "61")
@CardRegistration(set = "SLD", collectorNumber = "930")
public class GoblinSnowman extends Card {

    public GoblinSnowman() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, PreventDamageEffect.allCombatToSelf());
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, PreventDamageEffect.allCombatBySelf());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToTargetCreatureEffect(1)),
                "{T}: Goblin Snowman deals 1 damage to target creature it's blocking.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentBlockedBySourcePredicate()
                        )),
                        "Target must be a creature this creature is blocking"
                )
        ));
    }
}
