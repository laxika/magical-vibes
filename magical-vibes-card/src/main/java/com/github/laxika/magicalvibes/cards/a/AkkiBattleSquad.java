package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;

import java.util.List;

@CardRegistration(set = "NEC", collectorNumber = "18")
@CardRegistration(set = "NEC", collectorNumber = "57")
public class AkkiBattleSquad extends Card {

    public AkkiBattleSquad() {
        var modifiedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsModifiedPredicate()));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new OncePerTurnTriggerEffect(new ConditionalEffect(
                        new HasAttacker(modifiedCreature),
                        SequenceEffect.of(
                                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, modifiedCreature),
                                new AdditionalCombatPhaseEffect(1)))));
    }
}
