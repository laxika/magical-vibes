package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "96")
public class InciteWar extends Card {

    public InciteWar() {
        addCastingCost(EscalateCost.entwine("{2}"));

        var playerFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");

        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures target player controls attack this turn if able",
                        MatchingCreaturesMustAttackThisTurnEffect.controlledBy(Subjects.targetPlayer()), playerFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control gain first strike until end of turn",
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.otherCreaturesYouControl()))
        )));
    }
}
