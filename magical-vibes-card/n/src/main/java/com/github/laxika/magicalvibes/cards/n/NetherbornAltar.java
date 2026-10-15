package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCommanderIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "45")
public class NetherbornAltar extends Card {

    public NetherbornAltar() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PutCountersOnSelfEffect(CounterType.SOUL),
                        new PutCommanderIntoHandEffect(),
                        new LoseLifeEffect(new Scaled(new CountersOnSource(CounterType.SOUL), 3),
                                LoseLifeRecipient.CONTROLLER)
                ),
                "{T}, Put a soul counter on this artifact: Put your commander into your hand from the command zone. "
                        + "Then you lose 3 life for each soul counter on this artifact."
        ));
    }
}
