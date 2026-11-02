package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "56")
public class MaraudingSphinx extends Card {

    public MaraudingSphinx() {
        addTrigger(Triggers.youCommitCrime(),
                new OncePerTurnTriggerEffect(new SurveilEffect(2)));
    }
}
