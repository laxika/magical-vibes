package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "251")
@CardRegistration(set = "TDC", collectorNumber = "37")
@CardRegistration(set = "TDC", collectorNumber = "77")
public class RedoubledStormsinger extends Card {

    public RedoubledStormsinger() {
        // For each creature token you control that entered this turn, a tapped and attacking copy whose attack target
        // its controller chooses (CR 508.4), sacrificed at the beginning of the next end step.
        addTrigger(Triggers.attacks(), new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), null, null, Map.of(), false, false, true, true,
                false, false, null, Set.of(), false, List.of(), List.of(), false, false,
                new Fixed(1), false, Set.of(), true)
                .withSubject(Subjects.controlledBy(Subjects.you(), new PermanentAllOfPredicate(List.of(
                        new PermanentIsTokenPredicate(), new PermanentIsCreaturePredicate(),
                        new PermanentEnteredBattlefieldThisTurnPredicate())))));
    }
}
