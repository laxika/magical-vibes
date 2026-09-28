package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "7")
public class ImmovableRod extends Card {

    public ImmovableRod() {
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, new VentureIntoDungeonEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{W}",
                List.of(
                        new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.WHILE_SOURCE_REMAINS_TAPPED),
                        new LockTargetPermanentEffect(
                                true, true, false, EffectDuration.WHILE_SOURCE_REMAINS_TAPPED,
                                TargetPredicates.permanent())
                ),
                "{3}{W}, {T}: For as long as this artifact remains tapped, another target permanent loses all abilities and can't attack or block.",
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        "Target must be another permanent"
                )
        ));
    }
}
