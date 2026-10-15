package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "24")
public class MoradinsDisciples extends Card {

    public MoradinsDisciples() {
        // Whenever Moradin's Disciples attacks, tap target creature defending player controls.
        PermanentPredicate defendingPlayerCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByDefendingPlayerPredicate()));
        target(new PermanentPredicateTargetFilter(
                defendingPlayerCreature,
                "Target must be a creature defending player controls"))
                .addEffect(EffectSlot.ON_ATTACK,
                        new TapPermanentsEffect(TapUntapScope.TARGET, defendingPlayerCreature));
    }
}
