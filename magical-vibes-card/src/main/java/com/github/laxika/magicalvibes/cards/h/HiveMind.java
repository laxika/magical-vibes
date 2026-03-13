package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;

@CardRegistration(set = "M10", collectorNumber = "54")
public class HiveMind extends Card {

    public HiveMind() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new CopySpellForEachOtherPlayerEffect());
    }
}
