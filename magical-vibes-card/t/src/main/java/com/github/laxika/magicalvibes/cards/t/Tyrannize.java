package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "SHM", collectorNumber = "201")
public class Tyrannize extends Card {

    public Tyrannize() {
        // Target player discards their hand unless they pay 7 life.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"))
                .addSpellEffect(UnlessPaysEffect.unlessPaysLife(Subjects.targetPlayer(), 7,
                        new DiscardHandEffect(Subjects.targetPlayer())));
    }
}
