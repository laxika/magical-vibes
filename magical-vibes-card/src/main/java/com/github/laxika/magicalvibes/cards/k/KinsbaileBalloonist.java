package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

/**
 * Kinsbaile Balloonist — {3}{W} Creature — Kithkin Soldier 2/2
 *
 * Flying
 * Whenever this creature attacks, you may have target creature gain flying until end of turn.
 */
@CardRegistration(set = "LRW", collectorNumber = "23")
public class KinsbaileBalloonist extends Card {

    public KinsbaileBalloonist() {
        // Flying is loaded from Scryfall.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET),
                "Have target creature gain flying until end of turn?"
        ));
    }
}
