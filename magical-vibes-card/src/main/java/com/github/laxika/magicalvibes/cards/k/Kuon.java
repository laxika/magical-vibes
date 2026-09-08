package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CreatureDeathsThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;

@CardRegistration(set = "SOK", collectorNumber = "78")
public class Kuon extends Card {

    public Kuon() {
        setBackFaceCard(new KuonsEssence());

        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new CreatureDeathsThisTurnAtLeast(3),
                new TransformToBackFaceEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "KuonsEssence";
    }
}
