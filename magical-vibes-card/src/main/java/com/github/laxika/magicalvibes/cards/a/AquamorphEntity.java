package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PowerToughnessForm;
import com.github.laxika.magicalvibes.model.effect.ChoosePowerToughnessFormEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "33")
public class AquamorphEntity extends Card {

    public AquamorphEntity() {
        ChoosePowerToughnessFormEffect formChoice = new ChoosePowerToughnessFormEffect(List.of(
                new PowerToughnessForm("5/1", 5, 1),
                new PowerToughnessForm("1/5", 1, 5)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, formChoice);
        addMorph("{2}{U}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, formChoice);
    }
}
