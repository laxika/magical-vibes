package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "29")
@CardRegistration(set = "DRK", collectorNumber = "40")
@CardRegistration(set = "ME3", collectorNumber = "59")
public class Banshee extends Card {

    public Banshee() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(
                        DealDamageEffect.toAnyTarget(new Divided(new XValue(), 2)),
                        new DealDamageToPlayersEffect(new HalvedRoundedUp(new XValue()), Subjects.you())
                ),
                "{X}, {T}: This creature deals half X damage, rounded down, to any target, and half X damage, rounded up, to you."
        ));
    }
}
