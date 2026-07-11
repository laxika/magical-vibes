package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "215")
@CardRegistration(set = "M10", collectorNumber = "145")
@CardRegistration(set = "M11", collectorNumber = "147")
@CardRegistration(set = "9ED", collectorNumber = "200")
@CardRegistration(set = "POR", collectorNumber = "137")
@CardRegistration(set = "P02", collectorNumber = "107")
public class LavaAxe extends Card {

    public LavaAxe() {
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(5, DamageRecipient.TARGET_PLAYER));
    }
}
