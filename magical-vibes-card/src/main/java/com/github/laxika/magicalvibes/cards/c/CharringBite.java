package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

public class CharringBite extends Card {

    public CharringBite() {
        var withoutFlying = new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING));
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        withoutFlying
                )),
                "Target must be a creature without flying."
        )).addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(5, withoutFlying));
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
