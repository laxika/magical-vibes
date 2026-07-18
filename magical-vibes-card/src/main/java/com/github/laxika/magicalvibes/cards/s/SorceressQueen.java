package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "194")
@CardRegistration(set = "4ED", collectorNumber = "162")
public class SorceressQueen extends Card {

    public SorceressQueen() {
        // {T}: Target creature other than this creature has base power and toughness 0/2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SetBasePowerToughnessEffect(0, 2)),
                "{T}: Target creature other than this creature has base power and toughness 0/2 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        "Target must be another creature"
                )
        ));
    }
}
