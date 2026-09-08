package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "25")
public class ElectricEel extends Card {

    public ElectricEel() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{R}",
                List.of(
                        new BoostSelfEffect(2, 0),
                        new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)
                ),
                "{R}{R}: This creature gets +2/+0 until end of turn and deals 1 damage to you."
        ));
    }
}
