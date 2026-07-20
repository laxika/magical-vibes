package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "AKH", collectorNumber = "87")
public class DoomedDissenter extends Card {

    public DoomedDissenter() {
        // When Doomed Dissenter dies, create a 2/2 black Zombie creature token.
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.blackZombie(1));
    }
}
