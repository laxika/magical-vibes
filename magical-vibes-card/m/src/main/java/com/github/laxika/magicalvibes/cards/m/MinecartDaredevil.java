package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RideTheRails;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "141")
public class MinecartDaredevil extends Card {

    public MinecartDaredevil() {
        setBackFaceCard(new RideTheRails());
        addCastingOption(new AdventureCast("{1}{R}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "RideTheRails";
    }
}
