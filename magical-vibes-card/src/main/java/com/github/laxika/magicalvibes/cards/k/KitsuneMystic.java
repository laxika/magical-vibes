package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AutumnTailKitsuneSage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EnchantedByAtLeastAuras;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;

@CardRegistration(set = "CHK", collectorNumber = "28")
public class KitsuneMystic extends Card {

    public KitsuneMystic() {
        setBackFaceCard(new AutumnTailKitsuneSage());

        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new ConditionalEffect(new EnchantedByAtLeastAuras(2), new TransformToBackFaceEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "AutumnTailKitsuneSage";
    }
}
