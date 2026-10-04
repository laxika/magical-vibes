package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "M19", collectorNumber = "112")
@CardRegistration(set = "MIC", collectorNumber = "126")
@CardRegistration(set = "SCD", collectorNumber = "97")
public class OpenTheGraves extends Card {

    public OpenTheGraves() {
        // Whenever a nontoken creature you control dies, create a 2/2 black Zombie creature token.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, CreateTokenEffect.blackZombie(1));
    }
}
