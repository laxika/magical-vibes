package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Trigger descriptor for {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_SELF_CAST}
 * implementing the Cascade keyword (CR 702.85): "When you cast this spell, exile cards from the top
 * of your library until you exile a nonland card that costs less. You may cast it without paying its
 * mana cost. Put the exiled cards on the bottom of your library in a random order."
 *
 * <p>Placed in the {@code ON_SELF_CAST} slot, or in a cascade-granting marker slot, this is queued
 * as a plain triggered ability by {@code TriggerCollectionService.checkSpellCastTriggers} under the
 * caster, then resolved by {@code CascadeEffectHandler}: it digs the library until it exiles a
 * nonland card whose mana value is less than the cascade spell's mana value, then reuses the
 * {@link com.github.laxika.magicalvibes.model.LibrarySearchDestination#CAST_WITHOUT_PAYING} flow to
 * optionally cast that card for free and put the rest on the bottom in a random order.</p>
 *
 * <p>The {@link TriggeringSpellManaValueEffect} marker lets generic spell-cast triggers snapshot
 * the triggering spell's mana value for this effect.</p>
 *
 * @param instantOrSorceryOnly whether the qualifying card must be an instant or sorcery instead of
 *                             any nonland card
 * @param qualifyingCardFilter optional additional filter for the qualifying nonland card
 */
public record CascadeEffect(boolean instantOrSorceryOnly, CardPredicate qualifyingCardFilter)
        implements TriggeringSpellManaValueEffect {

    public CascadeEffect() {
        this(false, null);
    }

    public CascadeEffect(boolean instantOrSorceryOnly) {
        this(instantOrSorceryOnly, null);
    }

    public CascadeEffect(CardPredicate qualifyingCardFilter) {
        this(false, qualifyingCardFilter);
    }
}
