package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "83")
public class AppendageAmalgam extends Card {

    public AppendageAmalgam() {
        addTrigger(Triggers.attacks(), new SurveilEffect(1));
    }
}
