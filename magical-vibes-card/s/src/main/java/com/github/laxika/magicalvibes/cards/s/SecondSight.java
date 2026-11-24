package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateCost;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "33")
public class SecondSight extends Card {

    public SecondSight() {
        addCastingCost(EscalateCost.entwine("{U}"));
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Look at the top five cards of target opponent's library, then put them back in any order",
                        new ReorderTopCardsOfLibraryEffect(5, Subjects.targetPlayer()),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Look at the top five cards of your library, then put them back in any order",
                        new ReorderTopCardsOfLibraryEffect(5))
        )));
    }
}
