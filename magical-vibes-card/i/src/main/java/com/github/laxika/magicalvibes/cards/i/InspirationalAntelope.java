package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseLegacyWordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForChosenLegacyWordSpellsEffect;

@CardRegistration(set = "MB1", collectorNumber = "78")
public class InspirationalAntelope extends Card {

    public InspirationalAntelope() {
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new ChooseLegacyWordEffect(),
                "Choose a keyword or ability word for Inspirational Antelope?"));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForChosenLegacyWordSpellsEffect(1));
    }
}
