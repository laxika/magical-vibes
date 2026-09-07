package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "159")
public class PanickedAltisaur extends Card {

    public PanickedAltisaur() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT)),
                "{T}: This creature deals 2 damage to each opponent."
        ));
    }
}
