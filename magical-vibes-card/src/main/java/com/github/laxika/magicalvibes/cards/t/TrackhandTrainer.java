package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "YDFT", collectorNumber = "6")
public class TrackhandTrainer extends Card {

    public TrackhandTrainer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}{U}",
                List.of(new DrawCardEffect()),
                "{3}{U}{U}: Draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new ConjureCardToBattlefieldEffect("Training Grounds")),
                "Exhaust — {U}: Conjure a card named Training Grounds onto the battlefield."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
