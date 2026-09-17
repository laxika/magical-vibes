package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "239")
public class TimelessLotus extends Card {

    public TimelessLotus() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.WHITE),
                        new AwardManaEffect(ManaColor.BLUE),
                        new AwardManaEffect(ManaColor.BLACK),
                        new AwardManaEffect(ManaColor.RED),
                        new AwardManaEffect(ManaColor.GREEN)
                ),
                "{T}: Add {W}{U}{B}{R}{G}."
        ));
    }
}
