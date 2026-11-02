package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "108")
public class FleetingEffigy extends Card {

    public FleetingEffigy() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), ReturnToHandEffect.self());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new BoostSelfEffect(2, 0)),
                "{2}{R}: This creature gets +2/+0 until end of turn."
        ));
    }
}
