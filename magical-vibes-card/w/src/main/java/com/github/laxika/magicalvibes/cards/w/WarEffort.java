package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "131")
public class WarEffort extends Card {

    public WarEffort() {
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.ALL_OWN_CREATURES));
        addTrigger(Triggers.youAttack(),
                new CreateTokenEffect(
                1, "Warrior", 1, 1, CardColor.RED, List.of(CardSubtype.WARRIOR), true),
                new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
