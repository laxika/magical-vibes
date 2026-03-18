package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "ISD", collectorNumber = "114")
public class ScreechingBat extends Card {

    public ScreechingBat() {
        // Set up back face
        StalkingVampire backFace = new StalkingVampire();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // At the beginning of your upkeep, you may pay {2}{B}{B}. If you do, transform Screeching Bat.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayPayManaEffect("{2}{B}{B}", new TransformSelfEffect(),
                        "Pay {2}{B}{B} to transform Screeching Bat?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "StalkingVampire";
    }
}
