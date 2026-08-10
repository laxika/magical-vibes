package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "17")
public class Reconnaissance extends Card {

    public Reconnaissance() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new RemoveTargetFromCombatEffect(), new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{0}: Remove target attacking creature you control from combat and untap it.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsAttackingPredicate(),
                                new PermanentControlledBySourceControllerPredicate()
                        )),
                        "Target must be an attacking creature you control."
                )
        ));
    }
}
