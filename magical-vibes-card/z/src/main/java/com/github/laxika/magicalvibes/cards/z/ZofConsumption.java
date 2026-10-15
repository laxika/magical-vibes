package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "132")
public class ZofConsumption extends Card {

    public ZofConsumption() {
        setBackFaceCard(new ZofBloodbog());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Zof Consumption",
                        List.of(
                                new LoseLifeEffect(4, LoseLifeRecipient.EACH_OPPONENT),
                                new GainLifeEffect(4))),
                new ChooseOneEffect.ChooseOneOption("Zof Bloodbog", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "ZofBloodbog";
    }
}
