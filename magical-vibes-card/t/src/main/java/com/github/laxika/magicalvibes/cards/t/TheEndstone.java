package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.StartingLifeTotal;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "240")
public class TheEndstone extends Card {

    public TheEndstone() {
        addTrigger(Triggers.youCastSpell().anySpell(), new DrawCardEffect(1));
        addTrigger(Triggers.youPlayLand(), new DrawCardEffect(1));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new SetLifeTotalEffect(new HalvedRoundedUp(new StartingLifeTotal())));
    }
}
