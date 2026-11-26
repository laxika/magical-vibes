package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "PCY", collectorNumber = "76")
public class RhysticSyphon extends Card {

    public RhysticSyphon() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"))
                .addSpellEffect(UnlessPaysEffect.unlessPays(Subjects.targetPlayer(), "{3}", SequenceEffect.of(
                        new LoseLifeEffect(5, Subjects.targetPlayer()), new GainLifeEffect(5))));
    }
}
