package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ECC", collectorNumber = "21")
@CardRegistration(set = "ECC", collectorNumber = "41")
public class WickersmithsTools extends Card {

    public WickersmithsTools() {
        addEffect(EffectSlot.ON_MINUS_ONE_MINUS_ONE_COUNTERS_PUT_ON_CREATURE,
                new PutCountersOnSelfEffect(CounterType.CHARGE));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                new CountersOnSource(CounterType.CHARGE),
                                "Scarecrow",
                                2,
                                2,
                                null,
                                null,
                                List.of(CardSubtype.SCARECROW),
                                Set.of(),
                                Set.of(CardType.ARTIFACT),
                                false,
                                true,
                                Map.of(),
                                List.of(),
                                false,
                                false,
                                false,
                                0,
                                Set.of()
                        )
                ),
                "{5}, {T}, Sacrifice this artifact: Create X tapped 2/2 colorless Scarecrow artifact creature tokens, where X is the number of charge counters on this artifact."
        ));
    }
}
