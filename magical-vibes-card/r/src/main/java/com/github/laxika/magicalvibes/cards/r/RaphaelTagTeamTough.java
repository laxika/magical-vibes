package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

@CardRegistration(set = "TMC", collectorNumber = "118")
public class RaphaelTagTeamTough extends Card {

    public RaphaelTagTeamTough() {
        // "Whenever Raphael deals combat damage to a player for the first time each turn": use the
        // ally-combat-damage slot with a source-card predicate, then gate the single combined
        // ability with the existing once-per-turn trigger support.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new OncePerTurnTriggerEffect(new AllyCombatDamageTriggerEffect(
                        new PermanentIsSourceCardPredicate(),
                        SequenceEffect.of(
                                new UntapPermanentsEffect(
                                        TapUntapScope.CONTROLLED, new PermanentIsAttackingPredicate()),
                                new AdditionalCombatPhaseEffect(1)))));
    }
}
