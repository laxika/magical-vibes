package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

public class WaywardDisciple extends Card {

    private static final SequenceEffect DEATH_TRIGGER = SequenceEffect.of(
            new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
            new GainLifeEffect(1));

    public WaywardDisciple() {
        var trigger = target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));
        trigger.addEffect(EffectSlot.ON_DEATH, DEATH_TRIGGER);
        trigger.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, DEATH_TRIGGER);
    }
}
