package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessSacrificesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MKM", collectorNumber = "110")
@CardRegistration(set = "MKM", collectorNumber = "346")
@CardRegistration(set = "MKM", collectorNumber = "433")
public class VeinRipper extends Card {

    public VeinRipper() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessSacrificesEffect(new PermanentIsCreaturePredicate(), "creature"));

        // Whenever a creature dies (including this one), target opponent loses 2 life and you gain 2 life.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(2)));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ANY_CREATURE_DIES, SequenceEffect.of(
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(2)));
    }
}
