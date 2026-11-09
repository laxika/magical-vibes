package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker interface for effects whose primary action is countering a spell on the stack. Lets
 * consumers — chiefly the AI instant classifier — recognise a counterspell without listing every
 * concrete counter variant, mirroring how {@link ManaProducingEffect} marks mana production.
 *
 * <p>Descriptive only: membership is a fact about the effect, never a score.
 */
public interface CounterSpellingEffect extends CardEffect {

    /**
     * Whether a cast trigger with this effect goes on the stack with the triggering spell as its target, so it doesn't
     * resolve once that spell has left the stack (CR 608.2b). {@code false} for one that acts on "that spell" without
     * targeting it and still resolves after the spell was countered in response: it reveals, offers a free cast or
     * flips for its caster all the same (Counterbalance, Swindler's Scheme, Mirrored Depths).
     */
    default boolean targetsTriggeringSpell() {
        return true;
    }
}
