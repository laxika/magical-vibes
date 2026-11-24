package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSameNameAsEachPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "38")
public class Dichotomancy extends Card {

    public Dichotomancy() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(new SearchLibraryForSameNameAsEachPermanentEffect(
                Subjects.controlledBy(Subjects.targetPlayer(), new PermanentAllOfPredicate(List.of(
                        new PermanentIsTappedPredicate(),
                        new PermanentNotPredicate(new PermanentIsLandPredicate())))),
                Subjects.targetPlayerDeclaredElsewhere(), false, false, false));

        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{1}{U}{U}",
                List.of(),
                "Suspend 3—{1}{U}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(3));
    }
}
