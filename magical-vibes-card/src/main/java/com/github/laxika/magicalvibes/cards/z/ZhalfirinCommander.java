package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "49")
@CardRegistration(set = "TSB", collectorNumber = "18")
public class ZhalfirinCommander extends Card {

    private static final PermanentAllOfPredicate KNIGHT_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.KNIGHT)
    ));

    public ZhalfirinCommander() {
        // {1}{W}{W}: Target Knight creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}{W}",
                List.of(new BoostTargetCreatureEffect(1, 1, KNIGHT_CREATURE)),
                "{1}{W}{W}: Target Knight creature gets +1/+1 until end of turn.",
                new PermanentPredicateTargetFilter(KNIGHT_CREATURE, "Target must be a Knight creature")
        ));
    }
}
