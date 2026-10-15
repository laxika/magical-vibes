package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndDealDamageByHighestManaValueEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "147")
public class HereticsPunishment extends Card {

    public HereticsPunishment() {
        // {3}{R}: Choose any target, then mill three cards. Heretic's Punishment deals damage
        // to that permanent or player equal to the greatest mana value among the milled cards.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new MillControllerAndDealDamageByHighestManaValueEffect(3)),
                "{3}{R}: Choose any target, then mill three cards. Heretic's Punishment deals damage to that permanent or player equal to the greatest mana value among the milled cards."
        ));
    }
}
