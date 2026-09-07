package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "42")
public class SurgeOfRighteousness extends Card {

    public SurgeOfRighteousness() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK, CardColor.RED)),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentIsBlockingPredicate()))
                )),
                "Target must be a black or red creature that's attacking or blocking"
        ))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
    }
}
