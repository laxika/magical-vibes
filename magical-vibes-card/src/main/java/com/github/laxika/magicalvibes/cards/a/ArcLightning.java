package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "KTK", collectorNumber = "97")
@CardRegistration(set = "USG", collectorNumber = "174")
@CardRegistration(set = "BRB", collectorNumber = "5")
public class ArcLightning extends Card {

    public ArcLightning() {
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(3));
    }
}
