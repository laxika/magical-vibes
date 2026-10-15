package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "720")
public class Doorman extends Card {

    public Doorman() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentPowerAtLeastPredicate(3),
                                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL))
                        )),
                        "creatures with power 3 or greater that aren't Walls")),
                "{T}: Until end of turn, target creature can't be blocked by creatures with power 2 or less and/or Walls.",
                TargetFilters.creature()));
    }
}
