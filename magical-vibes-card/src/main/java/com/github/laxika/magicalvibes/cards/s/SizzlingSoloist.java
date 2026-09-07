package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureMustAttackNextCombatEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SNC", collectorNumber = "123")
public class SizzlingSoloist extends Card {

    public SizzlingSoloist() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                        SequenceEffect.of(
                                new CantBlockThisTurnEffect(TapUntapScope.TARGET),
                                ConditionalEffect.unless(
                                        new NthAbilityResolutionThisTurn(2),
                                        new TargetCreatureMustAttackNextCombatEffect())));
    }
}
