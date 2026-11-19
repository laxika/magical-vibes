package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "1")
public class BenalishKnightCounselor extends Card {

    public BenalishKnightCounselor() {
        addTrigger(Triggers.enlists(), RegisterDelayedControllerSpellCastTriggerEffect.boon(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect(new Fixed(1)))));
    }
}
