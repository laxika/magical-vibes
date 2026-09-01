package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "140")
public class UtopiaMycon extends Card {

    public UtopiaMycon() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.FUNGUS));

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

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.SAPROLING)
                                )),
                                "a Saproling",
                                false
                        ),
                        new AwardAnyColorManaEffect()
                ),
                "Sacrifice a Saproling: Add one mana of any color."
        ));
    }
}
