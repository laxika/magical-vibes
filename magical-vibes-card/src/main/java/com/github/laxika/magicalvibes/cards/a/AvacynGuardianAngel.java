package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToTargetFromChosenColorEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "3")
public class AvacynGuardianAngel extends Card {

    public AvacynGuardianAngel() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new PreventAllDamageToTargetFromChosenColorEffect()),
                "{1}{W}: Prevent all damage that would be dealt to another target creature this turn by sources of the color of your choice.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                        "Target must be another creature")));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{W}{W}",
                List.of(new PreventAllDamageToTargetFromChosenColorEffect()),
                "{5}{W}{W}: Prevent all damage that would be dealt to target player or planeswalker this turn by sources of the color of your choice.",
                new AnyTargetPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player or planeswalker")));
    }
}
