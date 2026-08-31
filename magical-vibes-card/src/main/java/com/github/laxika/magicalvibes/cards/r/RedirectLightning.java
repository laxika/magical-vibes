package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeOrPayManaCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "TLA", collectorNumber = "151")
public class RedirectLightning extends Card {

    public RedirectLightning() {
        addEffect(EffectSlot.SPELL, new PayLifeOrPayManaCost(5, "{2}"));
        target(new StackEntryPredicateTargetFilter(
                new StackEntryIsSingleTargetPredicate(),
                "Target spell or ability must have a single target."
        )).addEffect(EffectSlot.SPELL, new ChangeTargetOfTargetSpellWithSingleTargetEffect());
    }
}
