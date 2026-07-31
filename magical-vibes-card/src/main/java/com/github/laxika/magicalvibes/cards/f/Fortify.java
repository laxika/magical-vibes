package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "19")
public class Fortify extends Card {

    public Fortify() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +2/+0 until end of turn",
                        new BoostAllOwnCreaturesEffect(2, 0)),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +0/+2 until end of turn",
                        new BoostAllOwnCreaturesEffect(0, 2))
        )));
    }
}
