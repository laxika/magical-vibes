package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CON", collectorNumber = "136")
@CardRegistration(set = "SLD", collectorNumber = "2075")
public class FontOfMythos extends Card {

    public FontOfMythos() {
        // At the beginning of each player's draw step, that player draws two additional cards.
        addTrigger(Triggers.drawStep(ActivePlayer.ANY), new DrawCardForTargetPlayerEffect(2));
    }
}
