package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerMainPhase;
import com.github.laxika.magicalvibes.model.condition.ExactlyAttackers;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOC", collectorNumber = "69")
public class ValorsReach extends Card {

    public ValorsReach() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(
                        new ExactlyAttackers(2),
                        new GrantKeywordEffect(
                                Keyword.DOUBLE_STRIKE,
                                GrantScope.OWN_CREATURES,
                                new PermanentIsAttackingPredicate())));
        target(TargetFilters.creatureYouControl(), 0, 2)
                .addEffect(EffectSlot.CHAOS_TRIGGERED,
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new ConditionalEffect(
                        new ControllerMainPhase(),
                        new AdditionalCombatMainPhaseEffect(1)));
    }
}
