package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "146")
public class KazuulsFury extends Card {

    public KazuulsFury() {
        setBackFaceCard(new KazuulsCliffs());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Kazuul's Fury deals damage equal to the sacrificed creature's power to any target",
                        List.of(
                                new SacrificeCreatureCost(false, true),
                                new DealDamageToAnyTargetEffect(new XValue()))),
                new ChooseOneEffect.ChooseOneOption("Kazuul's Cliffs", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "KazuulsCliffs";
    }
}
