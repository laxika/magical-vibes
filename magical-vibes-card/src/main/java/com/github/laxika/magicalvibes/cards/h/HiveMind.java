package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "54")
public class HiveMind extends Card {

    public HiveMind() {
        // Whenever a player casts an instant or sorcery spell, each other player copies that spell.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new CopySpellForEachOtherPlayerEffect(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL))));
    }
}
