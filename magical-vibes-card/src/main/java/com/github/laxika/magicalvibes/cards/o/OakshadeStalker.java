package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MoonlitAmbusher;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "212")
public class OakshadeStalker extends Card {

    public OakshadeStalker() {
        setBackFaceCard(new MoonlitAmbusher());
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{G}")), null, true));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "MoonlitAmbusher";
    }
}
