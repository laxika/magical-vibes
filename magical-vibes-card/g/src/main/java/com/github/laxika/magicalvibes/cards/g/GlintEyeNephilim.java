package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "115")
@CardRegistration(set = "DMC", collectorNumber = "152")
@CardRegistration(set = "C16", collectorNumber = "201")
public class GlintEyeNephilim extends Card {

    public GlintEyeNephilim() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(new EventValue()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new BoostSelfEffect(1, 1)
                ),
                "{1}, Discard a card: This creature gets +1/+1 until end of turn."
        ));
    }
}
