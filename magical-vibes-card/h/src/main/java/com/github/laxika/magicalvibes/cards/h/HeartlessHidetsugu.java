package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "107")
@CardRegistration(set = "SLD", collectorNumber = "262")
@CardRegistration(set = "CMM", collectorNumber = "231")
@CardRegistration(set = "CMM", collectorNumber = "539")
public class HeartlessHidetsugu extends Card {

    public HeartlessHidetsugu() {
        // {T}: Heartless Hidetsugu deals damage to each player equal to half that player's life total, rounded down.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToPlayersEffect(new Divided(new ControllerLifeTotal(), 2),
                        Subjects.eachPlayer())),
                "{T}: Heartless Hidetsugu deals damage to each player equal to half that player's life total, rounded down."));
    }
}
