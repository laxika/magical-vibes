package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "26")
public class MakindiStampede extends Card {

    public MakindiStampede() {
        setBackFaceCard(new MakindiMesas());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +2/+2 until end of turn",
                        new BoostAllOwnCreaturesEffect(2, 2)).withManaCost("{3}{W}{W}"),
                new ChooseOneEffect.ChooseOneOption("Makindi Mesas", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "MakindiMesas";
    }
}
