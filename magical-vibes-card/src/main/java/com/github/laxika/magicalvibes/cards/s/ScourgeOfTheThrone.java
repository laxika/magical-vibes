package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAttacksPlayerWithMostLifePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "VMA", collectorNumber = "184")
public class ScourgeOfTheThrone extends Card {

    public ScourgeOfTheThrone() {
        addEffect(EffectSlot.ON_ATTACK, new OncePerTurnTriggerEffect(
                new TriggeringPermanentConditionalEffect(
                        new PermanentAttacksPlayerWithMostLifePredicate(),
                        SequenceEffect.of(
                                new UntapPermanentsEffect(
                                        TapUntapScope.ALL_CREATURES,
                                        new PermanentIsAttackingPredicate()),
                                new AdditionalCombatPhaseEffect(1)))));
    }
}
