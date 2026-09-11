package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Map;
import java.util.Set;

/**
 * Creates token(s) that are copies of the source permanent (the permanent with this ability).
 * The token copies all copiable characteristics per CR 707.2. When activated from the graveyard
 * (no source permanent), the copy is taken from the stack entry's card snapshot.
 *
 * <p>The color/subtype/mana-cost/P-T components apply the Embalm / Eternalize style "except it's a
 * &lt;color&gt; &lt;subtype&gt; ... with no mana cost" transformation to the copy; all are inert
 * ({@code null}/{@code false}) for a plain copy. Embalm keeps the source's P/T (both overrides
 * {@code null}); Eternalize sets a fixed base P/T (e.g. a 4/4 black Zombie).
 *
 * @param removeLegendary  if true, the token is not legendary (removes LEGENDARY supertype)
 * @param amount           number of token copies to create, evaluated when the effect resolves
 * @param colorOverride    if non-null, the token's color is set to exactly this color
 * @param addedSubtype     if non-null, this creature subtype is added to the copy (e.g. Zombie)
 * @param removeManaCost   if true, the token has no mana cost
 * @param powerOverride    if non-null, the token's base power is set to this (Eternalize 4/4)
 * @param toughnessOverride if non-null, the token's base toughness is set to this (Eternalize 4/4)
 * @param grantHaste       if true, the token gains haste
 * @param exileAtEndStep  if true, the token is exiled at the beginning of the next end step
 * @param initialCounters  counters placed on each token after it enters
 * @param tappedAndAttacking if true, the token enters tapped and attacking the same target as the source
 * @param additionalTypes card types added to the copy
 * @param startingLoyaltyOverride starting loyalty for a planeswalker token, if non-null
 * @param initialPlusOnePlusOneCounters +1/+1 counters with which each token enters
 * @param tapped             if true, the token enters tapped without entering attacking
 */
public record CreateTokenCopyOfSourceEffect(boolean removeLegendary, DynamicAmount amount,
                                            CardColor colorOverride, CardSubtype addedSubtype,
                                            boolean removeManaCost,
                                            Integer powerOverride, Integer toughnessOverride,
                                            boolean grantHaste, boolean exileAtEndStep,
                                            Map<CounterType, DynamicAmount> initialCounters,
                                            boolean tappedAndAttacking,
                                            Set<CardType> additionalTypes,
                                            Integer startingLoyaltyOverride,
                                            boolean tapped, int initialPlusOnePlusOneCounters)
        implements CardEffect {
        public CreateTokenCopyOfSourceEffect(boolean removeLegendary, DynamicAmount amount,
                                            CardColor colorOverride, CardSubtype addedSubtype,
                                            boolean removeManaCost,
                                            Integer powerOverride, Integer toughnessOverride,
                                            boolean grantHaste, boolean exileAtEndStep,
                                            Map<CounterType, DynamicAmount> initialCounters,
                                            boolean tappedAndAttacking,
                                            Set<CardType> additionalTypes) {
            this(removeLegendary, amount, colorOverride, addedSubtype, removeManaCost, powerOverride, toughnessOverride, grantHaste, exileAtEndStep, initialCounters, tappedAndAttacking, additionalTypes, null, false, 0);
        }


    /** Source copies that enter with the specified number of +1/+1 counters. */
    public CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount, int initialPlusOnePlusOneCounters) {
        this(removeLegendary, new Fixed(amount), null, null, false, null, null, false, false,
                Map.of(), false, Set.of(), null, false, initialPlusOnePlusOneCounters);
    }

    public CreateTokenCopyOfSourceEffect() {
        this(false, new Fixed(1), null, null, false, null, null, false, false, Map.of(), false,
                Set.of(), null, false, 0);
    }

    /** Backward-compatible: copies with an optional non-legendary flag and count, no transformation. */
    public CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount) {
        this(removeLegendary, new Fixed(amount), null, null, false, null, null, false, false,
                Map.of(), false, Set.of(), null, false, 0);
    }

    /** Source copy with a dynamically evaluated count. */
    public CreateTokenCopyOfSourceEffect(boolean removeLegendary, DynamicAmount amount) {
        this(removeLegendary, amount, null, null, false, null, null, false, false, Map.of(), false,
                Set.of(), null, false, 0);
    }

    /** Source copy with additional card types, such as an artifact copy of a creature. */
    public CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount,
                                         Set<CardType> additionalTypes) {
        this(removeLegendary, new Fixed(amount), null, null, false, null, null, false, false, Map.of(), false,
                additionalTypes, null, false, 0);
    }

    /** Plain source copy with optional haste and exile at the next end step. */
    public CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount,
                                         boolean grantHaste, boolean exileAtEndStep) {
        this(removeLegendary, new Fixed(amount), null, null, false, null, null, grantHaste, exileAtEndStep,
                Map.of(), false, Set.of(), null, false, 0);
    }

    /** Source copy with a dynamically evaluated count, optional haste, and next-end-step exile. */
    public CreateTokenCopyOfSourceEffect(boolean removeLegendary, DynamicAmount amount,
                                         boolean grantHaste, boolean exileAtEndStep) {
        this(removeLegendary, amount, null, null, false, null, null, grantHaste, exileAtEndStep,
                Map.of(), false, Set.of(), null, false, 0);
    }

    /** Creates a dynamic number of source copies that enter tapped and attacking. */
    public static CreateTokenCopyOfSourceEffect tappedAndAttacking(DynamicAmount amount,
                                                                    boolean exileAtEndStep) {
        return new CreateTokenCopyOfSourceEffect(
                false, amount, null, null, false, null, null, false, exileAtEndStep, Map.of(), true,
                Set.of(), null, false, 0);
    }

    /** Plain source copy with an explicit starting loyalty for planeswalker tokens. */
    public static CreateTokenCopyOfSourceEffect withStartingLoyalty(boolean removeLegendary, int amount,
                                                                   int startingLoyaltyOverride) {
        return new CreateTokenCopyOfSourceEffect(removeLegendary, new Fixed(amount), null, null, false, null, null, false, false,
                Map.of(), false, Set.of(), startingLoyaltyOverride, false, 0);
    }

    /** Embalm/Eternalize-style source copy with explicit power/toughness overrides. */
    public CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount,
                                         CardColor colorOverride, CardSubtype addedSubtype,
                                         boolean removeManaCost, Integer powerOverride,
                                         Integer toughnessOverride) {
        this(removeLegendary, new Fixed(amount), colorOverride, addedSubtype, removeManaCost,
                powerOverride, toughnessOverride, false, false, Map.of(), false, Set.of(), null, false, 0);
    }

    /** Embalm-style: color/subtype/no-mana-cost transform, keeps the source's P/T. */
    public CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount,
                                         CardColor colorOverride, CardSubtype addedSubtype,
                                         boolean removeManaCost) {
        this(removeLegendary, new Fixed(amount), colorOverride, addedSubtype, removeManaCost, null, null,
                false, false, Map.of(), false, Set.of(), null, false, 0);
    }

    /** Plain source copy that enters tapped and attacking with counters copied from the source. */
    public static CreateTokenCopyOfSourceEffect tappedAndAttackingWithSourceCounters() {
        return new CreateTokenCopyOfSourceEffect(
                false, new Fixed(1), null, null, false, null, null, false, false,
                Map.of(CounterType.PLUS_ONE_PLUS_ONE, new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)),
                true, Set.of(), null, false, 0);
    }

    /** Creates source copies that enter tapped without becoming attacking. */
    public static CreateTokenCopyOfSourceEffect tapped(int amount) {
        return new CreateTokenCopyOfSourceEffect(
                false, new Fixed(amount), null, null, false, null, null, false, false, Map.of(), false,
                Set.of(), null, true, 0);
    }
}
