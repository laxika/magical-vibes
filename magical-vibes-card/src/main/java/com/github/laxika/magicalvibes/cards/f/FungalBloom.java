package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "70")
public class FungalBloom extends Card {

    public FungalBloom() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{G}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.FUNGUS)),
                "{G}{G}: Put a spore counter on target Fungus.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.FUNGUS),
                        "Target must be a Fungus."
                )
        ));
    }
}
