package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "258")
public class CreepingMold extends Card {

    public CreepingMold() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(Set.of(
                CardType.ARTIFACT,
                CardType.ENCHANTMENT,
                CardType.LAND
        )));
    }
}
