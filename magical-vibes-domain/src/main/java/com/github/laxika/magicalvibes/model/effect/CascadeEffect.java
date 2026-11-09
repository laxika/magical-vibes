package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Trigger descriptor for {@code Triggers.youCastThisSpell()}
 * implementing the Cascade keyword (CR 702.85): "When you cast this spell, exile cards from the top
 * of your library until you exile a nonland card that costs less. You may cast it without paying its
 * mana cost. Put the exiled cards on the bottom of your library in a random order."
 *
 * <p>Placed in the {@code Triggers.youCastThisSpell()} ability, or in a cascade-granting marker slot, this is queued
 * as a plain triggered ability by {@code TriggerCollectionService.checkSpellCastTriggers} under the
 * caster, then resolved by {@code CascadeEffectHandler}: it digs the library until it exiles a
 * nonland card whose mana value is less than the cascade spell's mana value, then reuses the
 * {@link com.github.laxika.magicalvibes.model.LibrarySearchDestination#CAST_WITHOUT_PAYING} flow to
 * optionally cast that card for free and put the rest on the bottom in a random order.</p>
 *
 * <p>A cascade a cast trigger grants to another spell ("spells you cast with mana value 6 or greater have cascade")
 * compares with that spell's mana value as the ability resolves (the triggering spell, else as it last existed on the
 * stack, CR 608.2h); a spell's own cascade with its own.</p>
 *
 * @param instantOrSorceryOnly whether the qualifying card must be an instant or sorcery instead of
 *                             any nonland card
 * @param deferCastUntilEndOfTurn whether the hit instead receives a normal-timing free cast
 *                               permission until end of turn and remains exiled if not cast (Codie).
 * @param qualifyingCardFilter optional additional filter for the qualifying nonland card
 * @param declinedCardStaysExiled whether a hit the caster declines to cast is left exiled instead of
 *                                going to the bottom with the rest (Jodah, the Unifier: "Put the
 *                                rest on the bottom")
 */
public record CascadeEffect(boolean instantOrSorceryOnly, CardPredicate qualifyingCardFilter,
                             boolean deferCastUntilEndOfTurn, boolean declinedCardStaysExiled)
        implements CardEffect {

    public CascadeEffect(boolean instantOrSorceryOnly, CardPredicate qualifyingCardFilter,
                         boolean deferCastUntilEndOfTurn) {
        this(instantOrSorceryOnly, qualifyingCardFilter, deferCastUntilEndOfTurn, false);
    }

    public CascadeEffect(boolean instantOrSorceryOnly, CardPredicate qualifyingCardFilter) {
        this(instantOrSorceryOnly, qualifyingCardFilter, false, false);
    }

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
