package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "219")
public class OtherworldAtlas extends Card {

    public OtherworldAtlas() {
        // {T}: Put a charge counter on Otherworld Atlas.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCountersOnSelfEffect(CounterType.CHARGE)),
                "{T}: Put a charge counter on Otherworld Atlas."
        ));

        // {T}: Each player draws a card for each charge counter on Otherworld Atlas.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new EachPlayerDrawsCardEffect(new CountersOnSource(CounterType.CHARGE))),
                "{T}: Each player draws a card for each charge counter on Otherworld Atlas."
        ));
    }
}
