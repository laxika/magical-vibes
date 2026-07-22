package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsHandThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "149")
public class CollectiveDefiance extends Card {

    public CollectiveDefiance() {
        // Escalate {1} (Pay this cost for each mode chosen beyond the first.)
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{1}"));

        var playerFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");
        var creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");
        var opponentOrPlaneswalkerFilter = new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be an opponent or planeswalker.");

        // Choose one or more —
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player discards all the cards in their hand, then draws that many cards",
                        new TargetPlayerDiscardsHandThenDrawsThatManyEffect(),
                        playerFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Collective Defiance deals 4 damage to target creature",
                        new DealDamageToTargetCreatureEffect(4),
                        creatureFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Collective Defiance deals 3 damage to target opponent or planeswalker",
                        new DealDamageToTargetOpponentOrPlaneswalkerEffect(3),
                        opponentOrPlaneswalkerFilter)
        )));
    }
}
