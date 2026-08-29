package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "AKH", collectorNumber = "87")
@CardRegistration(set = "M19", collectorNumber = "93")
@CardRegistration(set = "AKR", collectorNumber = "101")
public class DoomedDissenter extends Card {

    public DoomedDissenter() {
        // When Doomed Dissenter dies, create a 2/2 black Zombie creature token.
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.blackZombie(1));
    }
}
