package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;

@CardRegistration(set = "TMP", collectorNumber = "314")
public class Watchdog extends Card {

    public Watchdog() {
        addEffect(EffectSlot.STATIC, new MustBlockEachCombatEffect());

        // As long as this creature is untapped, all creatures attacking you get -1/-0 — the
        // untapped gate is re-evaluated on every static pass, and the filter narrows the global
        // scope to creatures whose attack target is this creature's controller.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceUntapped(),
                new StaticBoostEffect(-1, 0, GrantScope.ALL_CREATURES,
                        new PermanentIsAttackingSourceControllerPredicate())));
    }
}
