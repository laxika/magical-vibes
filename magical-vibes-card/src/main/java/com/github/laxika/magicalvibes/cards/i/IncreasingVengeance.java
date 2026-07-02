package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "95")
public class IncreasingVengeance extends Card {

    public IncreasingVengeance() {
        // Copy target instant or sorcery spell you control.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                        new StackEntryControlledByPredicate()
                )),
                "Target must be an instant or sorcery spell you control."
        )).addEffect(EffectSlot.SPELL, new CopySpellEffect());

        // If this spell was cast from a graveyard, copy that spell twice instead (make a second copy).
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new CastFromZone(Zone.GRAVEYARD), new CopySpellEffect()));

        addCastingOption(new FlashbackCast("{3}{R}{R}"));
    }
}
