package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;

@CardRegistration(set = "AFR", collectorNumber = "73")
public class ShortcutSeeker extends Card {

    public ShortcutSeeker() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new VentureIntoDungeonEffect());
    }
}
