package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "23")
@CardRegistration(set = "NCC", collectorNumber = "124")
public class CephalidFacetaker extends Card {

    public CephalidFacetaker() {
        // This creature can't be blocked.
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());

        // At the beginning of combat on your turn, you may have this creature become a copy of
        // another target creature until end of turn, except it's 1/4 and has this ability.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another creature"
        )).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new MayEffect(
                SequenceEffect.of(
                        new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
                                1, 4, Set.of(), Set.of(), Set.of()),
                        new GrantStaticEffectToSourceUntilEndOfTurnEffect(new CantBeBlockedEffect())
                ),
                "Become a copy of the target creature?"));
    }
}
