package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "INR", collectorNumber = "105")
public class DesperateFarmer extends Card {

    public DesperateFarmer() {
        DepravedHarvester backFace = new DepravedHarvester();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // When another creature you control dies, transform this creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "DepravedHarvester";
    }
}
