package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SeasonOfTheWitchEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DRK", collectorNumber = "52")
public class SeasonOfTheWitch extends Card {

    public SeasonOfTheWitch() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayPayManaEffect(
                "{0}", null, "Pay 2 life to keep Season of the Witch?", MayPayPayer.CONTROLLER,
                new SacrificeSelfEffect(), 2));
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new SeasonOfTheWitchEffect());
    }
}
