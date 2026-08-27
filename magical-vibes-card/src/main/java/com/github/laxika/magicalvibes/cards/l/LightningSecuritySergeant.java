package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayWhileSourceControlledEffect;

@CardRegistration(set = "FIN", collectorNumber = "462")
@CardRegistration(set = "FIN", collectorNumber = "560")
public class LightningSecuritySergeant extends Card {

    public LightningSecuritySergeant() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopCardMayPlayWhileSourceControlledEffect());
    }
}
