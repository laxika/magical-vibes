package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Continuously grants a triggered ability to the permanents selected by {@code subject} (and,
 * for the multi-permanent scopes, {@code filter}). Added as a static ability.
 *
 * <p>{@code expirationCardId} names the exiled card a {@link Duration#WHILE_SOURCE_CARD_EXILED} grant depends on, or is
 * {@code null}.
 *
 * <p>{@code grantedAbility} is the granted ability — e.g. {@code (new TriggeredAbility(
 * Triggers.dealsDamageToOpponent(), new DrawCardEffect(1)), Subjects.sourceAndPaired())} for Tandem
 * Lookout's "each of those creatures has 'Whenever this creature deals damage to an opponent, draw
 * a card.'"
 *
 * <p>Unlike {@code GrantEffectToTargetUntilEndOfTurnEffect} the grant is not a one-shot
 * snapshot: it is recomputed by the layer system, so it turns on and off with its condition
 * (e.g. wrapped in {@code ConditionalEffect(new SourceIsPaired(), …)}).
 */
public record GrantTriggeredAbilityEffect(Ability grantedAbility,
                                          PermanentSubject subject,
                                          PermanentPredicate filter,
                                          UUID expirationCardId) implements CardEffect {

    /** The grant of {@code grantedAbility}. */
    public GrantTriggeredAbilityEffect(Ability grantedAbility, PermanentSubject subject) {
        this(grantedAbility, subject, null, null);
    }

    /** The grant of {@code grantedAbility} to the permanents {@code filter} matches. */
    public GrantTriggeredAbilityEffect(Ability grantedAbility, PermanentSubject subject, PermanentPredicate filter) {
        this(grantedAbility, subject, filter, null);
    }

    /**
     * A floating grant that lasts for as long as the card {@code expirationCardId} remains exiled
     * ({@link Duration#WHILE_SOURCE_CARD_EXILED}): cipher's "for as long as this card is encoded on that creature"
     * (CR 702.99a, 702.99c).
     */
    public static GrantTriggeredAbilityEffect whileCardExiled(Ability grantedAbility, UUID expirationCardId) {
        return new GrantTriggeredAbilityEffect(grantedAbility, Subjects.target(), null, expirationCardId);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
