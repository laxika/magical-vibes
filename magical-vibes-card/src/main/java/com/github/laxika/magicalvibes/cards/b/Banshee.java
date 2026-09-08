package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "29")
public class Banshee extends Card {

    public Banshee() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(
                        new DealDamageToAnyTargetEffect(new Divided(new XValue(), 2)),
                        new DealDamageToPlayersEffect(new HalvedRoundedUp(new XValue()), DamageRecipient.CONTROLLER)
                ),
                "{X}, {T}: This creature deals half X damage, rounded down, to any target, and half X damage, rounded up, to you."
        ));
    }
}
