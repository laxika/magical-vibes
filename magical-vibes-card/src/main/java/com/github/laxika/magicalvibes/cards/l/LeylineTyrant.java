package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfColorManaToDealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;

@CardRegistration(set = "ZNR", collectorNumber = "147")
public class LeylineTyrant extends Card {

    public LeylineTyrant() {
        addEffect(EffectSlot.STATIC, new PreventManaDrainEffect(ManaColor.RED));
        addEffect(EffectSlot.ON_DEATH,
                new PayAnyAmountOfColorManaToDealDamageToAnyTargetEffect(ManaColor.RED));
    }
}
