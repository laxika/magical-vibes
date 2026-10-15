package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "71")
public class DwarvenSeaClan extends Card {

    public DwarvenSeaClan() {
        // "{T}: Choose target attacking or blocking creature whose controller controls an Island.
        // This creature deals 2 damage to that creature at end of combat. Activate only before the
        // end of combat step."
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToTargetCreatureAtEndOfCombatEffect(2)),
                "{T}: Choose target attacking or blocking creature whose controller controls an Island. "
                        + "This creature deals 2 damage to that creature at end of combat. "
                        + "Activate only before the end of combat step.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsAttackingPredicate(),
                                        new PermanentIsBlockingPredicate()
                                )),
                                new PermanentControllerControlsPermanentPredicate(
                                        new PermanentHasSubtypePredicate(CardSubtype.ISLAND))
                        )),
                        "Target must be an attacking or blocking creature whose controller controls an Island"
                ),
                null,
                null,
                ActivationTimingRestriction.ONLY_BEFORE_END_OF_COMBAT
        ));
    }
}
