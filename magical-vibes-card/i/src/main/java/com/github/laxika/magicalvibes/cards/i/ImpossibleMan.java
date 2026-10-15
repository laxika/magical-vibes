package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "625")
public class ImpossibleMan extends Card {

    public ImpossibleMan() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect("Impossible Man", Set.of())),
                "{2}{U}: Impossible Man becomes a copy of another target permanent until end of turn, except his name is Impossible Man.",
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        "Target must be another permanent"
                )
        ));
    }
}
