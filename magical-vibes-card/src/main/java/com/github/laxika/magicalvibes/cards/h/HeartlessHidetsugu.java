package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachPlayerEqualToFractionOfLifeRoundedDownEffect;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "107")
public class HeartlessHidetsugu extends Card {

    public HeartlessHidetsugu() {
        // {T}: Heartless Hidetsugu deals damage to each player equal to half that player's life total, rounded down.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToEachPlayerEqualToFractionOfLifeRoundedDownEffect(2)),
                "{T}: Heartless Hidetsugu deals damage to each player equal to half that player's life total, rounded down."));
    }
}
