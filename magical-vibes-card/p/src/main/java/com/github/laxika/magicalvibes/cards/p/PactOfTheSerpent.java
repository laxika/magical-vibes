package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.NoteAmountEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "206")
@CardRegistration(set = "KHC", collectorNumber = "9")
public class PactOfTheSerpent extends Card {

    public PactOfTheSerpent() {
        addSpellEffect(new ChooseCreatureTypeAtResolutionEffect());
        addSpellEffect(new NoteAmountEffect(new PermanentCount(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentHasTypeChosenThisResolutionPredicate())),
                CountScope.TARGET_PLAYER)));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addSpellEffect(new DrawCardEffect(AmountThisWay.noted(), Subjects.targetPlayerDeclaredElsewhere()))
                .addSpellEffect(new LoseLifeEffect(AmountThisWay.noted(), Subjects.targetPlayer()));
    }
}
