package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOC", collectorNumber = "20")
@CardRegistration(set = "EOC", collectorNumber = "40")
public class EumidianHatchery extends Card {

    public EumidianHatchery() {
        // {T}, Pay 1 life: Add {B}. Put a hatchling counter on this land.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(1),
                        new AwardManaEffect(ManaColor.BLACK),
                        new PutCountersOnSelfEffect(CounterType.HATCHLING)
                ),
                "{T}, Pay 1 life: Add {B}. Put a hatchling counter on this land."
        ));

        // When this land is put into a graveyard from the battlefield, for each hatchling counter
        // on it, create a 1/1 black Insect creature token with flying.
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new CreateTokensForEachDyingSourceCounterEffect(
                        CounterType.HATCHLING,
                        new CreateTokenEffect("Insect", 1, 1, CardColor.BLACK,
                                List.of(CardSubtype.INSECT), Set.of(Keyword.FLYING), Set.of())));
    }
}
