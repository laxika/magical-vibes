package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "183")
public class WelderAutomaton extends Card {

    public WelderAutomaton() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)),
                "{3}{R}: This creature deals 1 damage to each opponent."
        ));
    }
}
