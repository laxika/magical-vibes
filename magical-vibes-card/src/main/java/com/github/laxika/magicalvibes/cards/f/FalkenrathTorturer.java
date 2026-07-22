package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "60")
@CardRegistration(set = "INR", collectorNumber = "109")
public class FalkenrathTorturer extends Card {

    public FalkenrathTorturer() {
        // Sacrifice a creature: This creature gains flying until end of turn.
        // If the sacrificed creature was a Human, put a +1/+1 counter on this creature.
        //
        // Modelled as two activated abilities so the +1/+1 counter is correctly tied to whether
        // the sacrificed creature was a Human, without the player being able to avoid the counter
        // by sacrificing a Human to the "no counter" variant.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)
                                )),
                                "Sacrifice a Human",
                                false
                        ),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                        new PutCountersOnSourceEffect(1, 1, 1)
                ),
                "Sacrifice a Human: Falkenrath Torturer gains flying until end of turn and gets a +1/+1 counter."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HUMAN))
                                )),
                                "Sacrifice a non-Human creature"
                        ),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)
                ),
                "Sacrifice a non-Human creature: Falkenrath Torturer gains flying until end of turn."
        ));
    }
}
