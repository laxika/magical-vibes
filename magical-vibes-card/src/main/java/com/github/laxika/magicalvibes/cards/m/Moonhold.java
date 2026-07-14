package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantCastCreatureSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantPlayLandsThisTurnEffect;
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
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.RED),
                new TargetPlayerCantPlayLandsThisTurnEffect()));

        // ... and can't cast creature spells this turn if {W} was spent to cast this spell.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.WHITE),
                new TargetPlayerCantCastCreatureSpellsThisTurnEffect()));
    }
}
