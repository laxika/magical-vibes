package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;
import java.util.Set;

/** Seeks one matching card into hand and applies perpetual card-identity modifications to it. */
public record SeekLibraryAndPerpetuallyModifySoughtCardEffect(
        CardPredicate filter,
        int maxManaValue,
        Set<Keyword> keywords,
        int genericCastCostReduction,
        Ability grantedAbility) implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public SeekLibraryAndPerpetuallyModifySoughtCardEffect(CardPredicate filter,
                                                           int maxManaValue,
                                                           Set<Keyword> keywords,
                                                           int genericCastCostReduction,
                                                           EffectSlot triggeredAbilitySlot,
                                                           CardEffect triggeredAbility) {
        Objects.requireNonNull(filter, "filter");
        if (maxManaValue < 0) {
            throw new IllegalArgumentException("maxManaValue cannot be negative");
        }
        keywords = Set.copyOf(keywords);
        if (genericCastCostReduction < 0) {
            throw new IllegalArgumentException("genericCastCostReduction cannot be negative");
        }
        if ((triggeredAbilitySlot == null) != (triggeredAbility == null)) {
            throw new IllegalArgumentException(
                    "Triggered ability slot and effect must be provided together");
        }
        this(filter, maxManaValue, keywords, genericCastCostReduction, LegacySlots.grant(triggeredAbilitySlot, triggeredAbility));
    }

    /** The slot of {@link #grantedAbility}, or {@code null} when there is none. */
    public EffectSlot triggeredAbilitySlot() {
        return grantedAbility == null ? null : LegacySlots.slotOf(grantedAbility);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredAbility() {
        return grantedAbility == null ? null : LegacySlots.effectsOf(grantedAbility).getFirst();
    }
}
