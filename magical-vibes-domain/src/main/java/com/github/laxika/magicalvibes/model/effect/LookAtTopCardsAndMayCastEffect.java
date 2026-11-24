package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * "Look at [reveal] the top N cards of your library. You may cast a [kind of] spell [with mana value M or less] from
 * among them without paying its mana cost. Put the rest on the bottom of your library in a random order." The count
 * and the mana-value bound are evaluated once as the effect resolves (CR 608.2h); a spell chosen is cast then and
 * there (CR 608.2g), its additional costs still paid (CR 118.8). The looked-at cards stay in the library while the
 * choice is made: looking at or revealing a card doesn't move it (CR 701.20b, 701.20e), so the spell is cast from the
 * library.
 *
 * <p>A land can't be cast, so a land face is never offered. A modal double-faced card is offered when either face
 * matches, its caster choosing the face as they cast it (CR 712.8b). The mana-value bound is a bound on the spell: a
 * split card is offered when one of its halves is within it, and only such a half may be cast (CR 709.3a).
 *
 * @param count                the number of cards looked at, capped by the library's size
 * @param castFilter           what the spell must be ("an instant or sorcery spell"), {@code null} for any spell
 * @param spellManaValueAtMost the most the spell's mana value may be ("less than X" is {@code X - 1}),
 *                             {@code null} for no bound
 * @param reveals              whether the cards are revealed to every player rather than looked at
 * @param restOnBottom         whether the cards not cast go to the bottom of the library in a random order; when
 *                             {@code false} nothing moves them ("look at the top card ... you may cast it")
 */
public record LookAtTopCardsAndMayCastEffect(DynamicAmount count, CardPredicate castFilter,
                                             DynamicAmount spellManaValueAtMost, boolean reveals,
                                             boolean restOnBottom) implements CardEffect {

    public LookAtTopCardsAndMayCastEffect {
        Objects.requireNonNull(count, "count");
    }

    /** "Look at the top {@code count} cards of your library. You may cast a spell from among them …" */
    public static LookAtTopCardsAndMayCastEffect lookAt(DynamicAmount count) {
        return new LookAtTopCardsAndMayCastEffect(count, null, null, false, true);
    }

    /** As {@link #lookAt(DynamicAmount)} with a fixed count. */
    public static LookAtTopCardsAndMayCastEffect lookAt(int count) {
        return lookAt(new Fixed(count));
    }

    /** "Reveal the top {@code count} cards of your library. You may cast a spell from among cards revealed this way …" */
    public static LookAtTopCardsAndMayCastEffect reveal(DynamicAmount count) {
        return new LookAtTopCardsAndMayCastEffect(count, null, null, true, true);
    }

    /** "… cast an instant or sorcery spell …": only a spell matching {@code filter} may be cast. */
    public LookAtTopCardsAndMayCastEffect matching(CardPredicate filter) {
        return new LookAtTopCardsAndMayCastEffect(count, filter, spellManaValueAtMost, reveals, restOnBottom);
    }

    /** "… a spell with mana value {@code bound} or less …". */
    public LookAtTopCardsAndMayCastEffect withSpellManaValueAtMost(DynamicAmount bound) {
        return new LookAtTopCardsAndMayCastEffect(count, castFilter, Objects.requireNonNull(bound, "bound"), reveals,
                restOnBottom);
    }

    /** No "put the rest on the bottom": a card not cast stays where it is (Galvanoth, Planetarium of Wan Shi Tong). */
    public LookAtTopCardsAndMayCastEffect leavingTheRest() {
        return new LookAtTopCardsAndMayCastEffect(count, castFilter, spellManaValueAtMost, reveals, false);
    }
}
