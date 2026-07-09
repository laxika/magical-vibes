package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper effect: "Clash with an opponent. If you win, [wrapped effect]."
 * (MTG rule 701.29). At resolution the controller clashes against their (2-player) opponent —
 * both reveal the top card of their library, the controller wins if their revealed card's mana
 * value is strictly greater — and the {@code wrapped} effect is dispatched only on a win.
 *
 * <p>This is the clash-<em>source</em> counterpart to {@link IfWonClashEffect}: this effect
 * <em>initiates</em> a clash from a spell/ability resolution, whereas {@code IfWonClashEffect}
 * is a "whenever you clash" trigger clause consumed by {@code TriggerCollectionService}.
 *
 * @param wrapped the effect to execute when the controller wins the clash (may be {@code null}
 *                for a bare "clash with an opponent" that has no win reward)
 */
public record ClashEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return wrapped != null && wrapped.canTargetPermanent();
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped != null && wrapped.canTargetPlayer();
    }
}
