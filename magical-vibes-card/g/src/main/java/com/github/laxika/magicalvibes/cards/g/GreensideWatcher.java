package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "122")
public class GreensideWatcher extends Card {

    public GreensideWatcher() {
        // {T}: Untap target Gate.
        var gate = new PermanentHasSubtypePredicate(CardSubtype.GATE);
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, gate)),
                "{T}: Untap target Gate.",
                new PermanentPredicateTargetFilter(gate, "Target must be a Gate")
        ));
    }
}
