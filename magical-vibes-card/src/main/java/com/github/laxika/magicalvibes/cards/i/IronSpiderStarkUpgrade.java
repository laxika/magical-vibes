package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "166")
public class IronSpiderStarkUpgrade extends Card {

    public IronSpiderStarkUpgrade() {
        // {T}: Put a +1/+1 counter on each artifact creature and/or Vehicle you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        1,
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate()
                                )),
                                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)
                        ))
                )),
                "{T}: Put a +1/+1 counter on each artifact creature and/or Vehicle you control."
        ));

        // {2}, Remove two +1/+1 counters from among artifacts you control: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RemoveCounterFromControlledPermanentCost(
                                List.of(CounterType.PLUS_ONE_PLUS_ONE),
                                2,
                                new PermanentIsArtifactPredicate(),
                                false
                        ),
                        new DrawCardEffect(1)
                ),
                "{2}, Remove two +1/+1 counters from among artifacts you control: Draw a card."
        ));
    }
}
