package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PowerToughnessForm;
import com.github.laxika.magicalvibes.model.effect.ChoosePowerToughnessFormEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "59")
public class PrimalPlasma extends Card {

    public PrimalPlasma() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChoosePowerToughnessFormEffect(List.of(
                new PowerToughnessForm("3/3", 3, 3),
                new PowerToughnessForm("2/2 with flying", 2, 2, Set.of(Keyword.FLYING)),
                new PowerToughnessForm("1/6 with defender", 1, 6, Set.of(Keyword.DEFENDER)))));
    }
}
