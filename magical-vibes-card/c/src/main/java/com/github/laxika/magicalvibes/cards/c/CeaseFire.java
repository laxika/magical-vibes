package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;


@CardRegistration(set = "ODY", collectorNumber = "14")
public class CeaseFire extends Card {

    public CeaseFire() {
        // Target player can't cast creature spells this turn.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addSpellEffect(PlayerRestrictionEffect.thisTurn(Subjects.targetPlayer(),
                        PlayerRestriction.castingSpellsOf(CardType.CREATURE)))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
