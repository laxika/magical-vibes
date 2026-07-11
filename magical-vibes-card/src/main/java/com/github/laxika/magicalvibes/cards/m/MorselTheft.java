package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.CastForProwlCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "68")
public class MorselTheft extends Card {

    public MorselTheft() {
        // Prowl {1}{B}: cast for this cost if you dealt combat damage to a player this turn with a Rogue.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{B}")), CardSubtype.ROGUE));

        // Target player loses 3 life and you gain 3 life.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ))
                .addEffect(EffectSlot.SPELL, new TargetPlayerLosesLifeAndControllerGainsLifeEffect(3, 3));

        // If this spell's prowl cost was paid, draw a card.
        addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new CastForProwlCost(), new DrawCardEffect()));
    }
}
