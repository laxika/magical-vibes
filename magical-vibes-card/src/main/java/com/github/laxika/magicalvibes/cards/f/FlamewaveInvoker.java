package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "202")
@CardRegistration(set = "LGN", collectorNumber = "92")
@CardRegistration(set = "DD1", collectorNumber = "36")
@CardRegistration(set = "DD2", collectorNumber = "40")
@CardRegistration(set = "JVC", collectorNumber = "40")
@CardRegistration(set = "EVG", collectorNumber = "36")
public class FlamewaveInvoker extends Card {

    public FlamewaveInvoker() {
        addActivatedAbility(new ActivatedAbility(false, "{7}{R}", List.of(new DealDamageToPlayersEffect(5, DamageRecipient.TARGET_PLAYER)), "{7}{R}: Flamewave Invoker deals 5 damage to target player or planeswalker."));
    }
}
