package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "30")
public class OnduInversion extends Card {

    public OnduInversion() {
        OnduSkyruins backFace = new OnduSkyruins();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Ondu Inversion",
                        List.of(new DestroyAllPermanentsEffect(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()))))
                        .withManaCost("{6}{W}{W}"),
                new ChooseOneEffect.ChooseOneOption("Ondu Skyruins", List.of())
        )));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "OnduSkyruins";
    }
}
