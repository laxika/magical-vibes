package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AzantaTheSunkenRuin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "74")
public class SearchForAzcanta extends Card {

    public SearchForAzcanta() {
        setBackFaceCard(new AzantaTheSunkenRuin());

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SequenceEffect(List.of(
                new SurveilEffect(1),
                new ConditionalEffect(
                        new GraveyardCardThreshold(7, null),
                        new MayEffect(new TransformSelfEffect(), "Transform Search for Azcanta?"),
                        false))));
    }

    @Override
    public String getBackFaceClassName() {
        return "AzantaTheSunkenRuin";
    }
}
