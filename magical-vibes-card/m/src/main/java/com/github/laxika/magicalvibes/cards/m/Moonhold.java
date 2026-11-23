package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "EVE", collectorNumber = "143")
public class Moonhold extends Card {

    public Moonhold() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ));

        // Target player can't play lands this turn if {R} was spent to cast this spell.
        addSpellEffect(new ConditionalEffect(
                new ColorSpentToCast(ManaColor.RED),
                PlayerRestrictionEffect.thisTurn(Subjects.targetPlayerDeclaredElsewhere(),
                        PlayerRestriction.playingLands())));

        // ... and can't cast creature spells this turn if {W} was spent to cast this spell.
        addSpellEffect(new ConditionalEffect(
                new ColorSpentToCast(ManaColor.WHITE),
                PlayerRestrictionEffect.thisTurn(Subjects.targetPlayer(),
                        PlayerRestriction.castingSpellsOf(CardType.CREATURE))));
    }
}
