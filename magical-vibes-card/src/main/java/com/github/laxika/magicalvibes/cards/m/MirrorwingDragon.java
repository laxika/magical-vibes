package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;

@CardRegistration(set = "INR", collectorNumber = "165")
public class MirrorwingDragon extends Card {

    public MirrorwingDragon() {
        // Whenever a player casts an instant or sorcery spell that targets only this creature,
        // that player copies that spell for each other creature they control that the spell could target.
        // Each copy targets a different one of those creatures.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new CopySpellForEachOtherControlledCreatureEffect());
    }
}
