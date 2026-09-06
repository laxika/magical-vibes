package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromZonesEffect;

import java.util.EnumSet;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "11")
public class DrannithMagistrate extends Card {

    public DrannithMagistrate() {
        Set<Zone> nonHandZones = Set.copyOf(EnumSet.complementOf(EnumSet.of(Zone.HAND)));
        addEffect(EffectSlot.STATIC, new PlayersCantCastSpellsFromZonesEffect(nonHandZones, false));
    }
}
