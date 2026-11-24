package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "196")
@CardRegistration(set = "MOM", collectorNumber = "383")
@CardRegistration(set = "SOC", collectorNumber = "273")
public class KamiOfWhisperedHopes extends Card {

    public KamiOfWhisperedHopes() {
        addStaticEffect(AddOnePlusOneCountersEffect.plusOne(CounterType.PLUS_ONE_PLUS_ONE, AddOnePlusOneCountersEffect.youControl()));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(new SourcePower())),
                "{T}: Add X mana of any one color, where X is this creature's power."
        ));
    }
}
