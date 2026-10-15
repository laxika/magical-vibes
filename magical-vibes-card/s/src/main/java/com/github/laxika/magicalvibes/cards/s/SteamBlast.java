package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "USG", collectorNumber = "219")
@CardRegistration(set = "BRB", collectorNumber = "81")
public class SteamBlast extends Card {

    public SteamBlast() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(2, true));
    }
}
