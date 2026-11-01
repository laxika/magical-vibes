package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureMustAttackNextCombatEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "123")
public class SizzlingSoloist extends Card {

    public SizzlingSoloist() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                        SequenceEffect.of(
                                new CantBlockThisTurnEffect(TapUntapScope.TARGET),
                                ConditionalEffect.unless(
                                        new NthAbilityResolutionThisTurn(2),
                                        new TargetCreatureMustAttackNextCombatEffect())));
    }
}
