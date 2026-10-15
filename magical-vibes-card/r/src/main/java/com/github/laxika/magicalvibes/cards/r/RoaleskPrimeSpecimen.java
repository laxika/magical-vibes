package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PayXManaConjureRandomCreatureWithManaValueAndCloakEffect;

@CardRegistration(set = "YMKM", collectorNumber = "26")
public class RoaleskPrimeSpecimen extends Card {

    public RoaleskPrimeSpecimen() {
        addMorph("{G}{U}");
        addEffect(EffectSlot.ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP,
                new PayXManaConjureRandomCreatureWithManaValueAndCloakEffect());
    }
}
