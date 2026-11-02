package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "54")
@CardRegistration(set = "SLD", collectorNumber = "822")
public class HiveMind extends Card {

    public HiveMind() {
        // Whenever a player casts an instant or sorcery spell, each other player copies that spell.
        addTrigger(Triggers.playerCastsSpell(),
                new CopySpellForEachOtherPlayerEffect(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL))));
    }
}
