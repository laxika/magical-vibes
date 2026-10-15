package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "34")
public class CurseOfEchoes extends Card {

    public CurseOfEchoes() {
        // Whenever enchanted player casts an instant or sorcery spell, each other player
        // may copy that spell and may choose new targets for the copy they control.
        // Reuses Hive Mind's ON_ANY_PLAYER_CASTS_SPELL copy machinery; the filter restricts
        // firing to instant/sorcery spells cast by the enchanted player.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new CopySpellForEachOtherPlayerEffect(true,
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                                new StackEntryControlledByEnchantedPlayerPredicate()))));
    }
}
