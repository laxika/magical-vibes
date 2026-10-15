package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "TMC", collectorNumber = "110")
public class DonatellosScienceLesson extends Card {

    public DonatellosScienceLesson() {
        target(creatureOrPlayer(), 0, 4)
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL, new DrawCardForTargetPlayerEffect(1, false, true));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_TWO_CREATURES_AND_TWO_PLAYERS);
    }

    private static AnyTargetPredicateTargetFilter creatureOrPlayer() {
        return new AnyTargetPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a creature or player");
    }

}
