package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ARN", collectorNumber = "6")
public class KingSuleiman extends Card {

    public KingSuleiman() {
        var djinnOrEfreet = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.DJINN),
                new PermanentHasSubtypePredicate(CardSubtype.EFREET)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DestroyTargetPermanentEffect(djinnOrEfreet)),
                "{T}: Destroy target Djinn or Efreet.",
                new PermanentPredicateTargetFilter(djinnOrEfreet, "Target must be a Djinn or Efreet")
        ));
    }
}
