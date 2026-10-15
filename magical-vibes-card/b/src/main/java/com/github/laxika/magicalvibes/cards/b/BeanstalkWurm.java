package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PlantBeans;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "161")
public class BeanstalkWurm extends Card {

    public BeanstalkWurm() {
        setBackFaceCard(new PlantBeans());
        addCastingOption(new AdventureCast("{1}{G}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "PlantBeans";
    }
}
