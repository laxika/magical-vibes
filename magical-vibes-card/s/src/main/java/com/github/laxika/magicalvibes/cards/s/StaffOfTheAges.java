package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LandwalkIgnoredForBlockingEffect;

@CardRegistration(set = "ICE", collectorNumber = "340")
public class StaffOfTheAges extends Card {

    public StaffOfTheAges() {
        // Creatures with landwalk abilities can be blocked as though they didn't have those abilities.
        addEffect(EffectSlot.STATIC, new LandwalkIgnoredForBlockingEffect());
    }
}
