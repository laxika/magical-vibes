package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreaturesWithCounterAttackTogetherEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesWithCounterMustBlockTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "295")
public class MagneticWeb extends Card {

    public MagneticWeb() {
        // If a creature with a magnet counter on it attacks, all creatures with magnet counters on
        // them attack if able.
        addEffect(EffectSlot.STATIC, new CreaturesWithCounterAttackTogetherEffect(CounterType.MAGNET));

        // Whenever a creature with a magnet counter on it attacks, all creatures with magnet
        // counters on them block that creature this turn if able.
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new TriggeringPermanentConditionalEffect(
                new PermanentHasCountersPredicate(CounterType.MAGNET),
                new CreaturesWithCounterMustBlockTriggeringAttackerEffect(CounterType.MAGNET)));

        // {1}, {T}: Put a magnet counter on target creature.
        addActivatedAbility(new ActivatedAbility(true, "{1}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.MAGNET, 1)),
                "{1}, {T}: Put a magnet counter on target creature.",
                TargetFilters.creature()));
    }
}
