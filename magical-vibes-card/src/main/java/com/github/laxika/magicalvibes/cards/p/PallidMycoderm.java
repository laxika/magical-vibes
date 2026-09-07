package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "10")
public class PallidMycoderm extends Card {

    public PallidMycoderm() {
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
                "Remove three spore counters from Pallid Mycoderm: Create a 1/1 green Saproling creature token."
        ));

        PermanentPredicate saproling = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.SAPROLING)
        ));
        PermanentPredicate fungusOrSaproling = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.FUNGUS),
                new PermanentHasSubtypePredicate(CardSubtype.SAPROLING)
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(saproling, "a Saproling", false),
                        new BoostAllOwnCreaturesEffect(1, 1, fungusOrSaproling)
                ),
                "Sacrifice a Saproling: Each creature you control that's a Fungus or a Saproling gets +1/+1 until end of turn."
        ));
    }
}
