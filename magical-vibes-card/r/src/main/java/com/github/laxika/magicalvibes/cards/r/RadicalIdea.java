package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.JumpStartCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "GRN", collectorNumber = "52")
@CardRegistration(set = "RVR", collectorNumber = "58")
@CardRegistration(set = "OTC", collectorNumber = "110")
public class RadicalIdea extends Card {

    public RadicalIdea() {
        addSpellEffect(new DrawCardEffect(1));
        addCastingOption(new JumpStartCast());
    }
}
