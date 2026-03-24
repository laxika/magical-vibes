package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AzantaTheSunkenRuin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerGraveyardCardThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "XLN", collectorNumber = "74")
public class SearchForAzcanta extends Card {

    public SearchForAzcanta() {
        // Set up back face
        AzantaTheSunkenRuin backFace = new AzantaTheSunkenRuin();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // At the beginning of your upkeep, surveil 1.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SurveilEffect(1));

        // Then if you have seven or more cards in your graveyard,
        // you may transform Search for Azcanta.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ControllerGraveyardCardThresholdConditionalEffect(7, null,
                        new MayEffect(new TransformSelfEffect(),
                                "Transform Search for Azcanta?")));
    }

    @Override
    public String getBackFaceClassName() {
        return "AzantaTheSunkenRuin";
    }
}
