package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDefendingPlayerCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "131")
public class GougedZealot extends Card {

    public GougedZealot() {
        addTrigger(Triggers.attacks(), new ConditionalEffect(new Delirium(),
                new DealDamageToDefendingPlayerCreaturesEffect(1)));
    }
}
