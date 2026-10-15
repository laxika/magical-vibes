package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "632")
public class NamoraTheSeaQueen extends Card {

    public NamoraTheSeaQueen() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new CreateTokenEffect(2, "Merfolk", 1, 1, CardColor.BLUE,
                                List.of(CardSubtype.MERFOLK), Set.of(), Set.of())
                ),
                "Power-up — {5}{U}: Put a +1/+1 counter on Namora. Create two 1/1 blue Merfolk creature tokens. "
                        + "Activate each power-up ability only once. Reduce the cost by her mana cost if she entered this turn."
        ).withPowerUp());
    }
}
