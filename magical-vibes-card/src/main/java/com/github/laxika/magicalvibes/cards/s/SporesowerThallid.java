package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "220")
public class SporesowerThallid extends Card {

    public SporesowerThallid() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCounterOnEachControlledPermanentEffect(
                CounterType.FUNGUS,
                1,
                new PermanentHasSubtypePredicate(CardSubtype.FUNGUS)
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.FUNGUS),
                        new CreateTokenEffect(
                                "Saproling",
                                1,
                                1,
                                CardColor.GREEN,
                                List.of(CardSubtype.SAPROLING),
                                Set.of(),
                                Set.of()
                        )
                ),
                "Remove three spore counters from this creature: Create a 1/1 green Saproling creature token."
        ));
    }
}
