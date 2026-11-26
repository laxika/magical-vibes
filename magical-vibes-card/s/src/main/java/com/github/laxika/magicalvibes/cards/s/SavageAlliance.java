package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "169")
@CardRegistration(set = "EMN", collectorNumber = "140")
public class SavageAlliance extends Card {

    public SavageAlliance() {
        setAllowSharedTargets(true);

        // Escalate {1} (Pay this cost for each mode chosen beyond the first.)
        addCastingCost(EscalateCost.escalate("{1}"));

        var playerFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");
        var creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");
        var opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");

        // Choose one or more —
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures target player controls gain trample until end of turn",
                        new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.creaturesTargetPlayerControls()),
                        playerFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Savage Alliance deals 2 damage to target creature",
                        DealDamageEffect.toTargetCreature(2),
                        creatureFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Savage Alliance deals 1 damage to each creature target opponent controls",
                        DealDamageEffect.toEach(
                                Subjects.controlledBy(Subjects.targetPlayer(), new PermanentIsCreaturePredicate()),
                                1),
                        opponentFilter)
        )));
    }
}
