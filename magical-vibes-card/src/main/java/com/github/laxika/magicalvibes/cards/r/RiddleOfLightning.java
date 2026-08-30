package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardDealManaValueDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "JOU", collectorNumber = "107")
@CardRegistration(set = "FUT", collectorNumber = "105")
public class RiddleOfLightning extends Card {

    public RiddleOfLightning() {
        addEffect(EffectSlot.SPELL, new ScryEffect(3));
        addEffect(EffectSlot.SPELL, new RevealTopCardDealManaValueDamageToAnyTargetEffect());
    }
}
