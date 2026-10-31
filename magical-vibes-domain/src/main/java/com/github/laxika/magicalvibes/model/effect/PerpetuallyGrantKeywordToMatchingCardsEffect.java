package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/** Perpetually grants keywords to matching cards in configured zones and matching permanents on the battlefield. */
public record PerpetuallyGrantKeywordToMatchingCardsEffect(
        CardPredicate handFilter,
        PermanentPredicate permanentFilter,
        Set<Keyword> keywords,
        Ability grantedAbility,
        CardPredicate graveyardFilter,
        CardPredicate libraryFilter) implements KeywordGrantingEffect {

    /** The grant as the slot and effect that stand for it. */
    public PerpetuallyGrantKeywordToMatchingCardsEffect(CardPredicate handFilter,
                                                        PermanentPredicate permanentFilter,
                                                        Set<Keyword> keywords,
                                                        EffectSlot triggeredAbilitySlot,
                                                        CardEffect triggeredAbility,
                                                        CardPredicate graveyardFilter,
                                                        CardPredicate libraryFilter) {
        keywords = Set.copyOf(keywords);
        if ((triggeredAbilitySlot == null) != (triggeredAbility == null)) {
            throw new IllegalArgumentException("Triggered ability slot and effect must be provided together");
        }
        this(handFilter, permanentFilter, keywords, LegacySlots.grant(triggeredAbilitySlot, triggeredAbility), graveyardFilter, libraryFilter);
    }

    public PerpetuallyGrantKeywordToMatchingCardsEffect(CardPredicate handFilter,
                                                         PermanentPredicate permanentFilter,
                                                         Set<Keyword> keywords) {
        this(handFilter, permanentFilter, keywords, null, null, null, null);
    }

    public PerpetuallyGrantKeywordToMatchingCardsEffect(CardPredicate handFilter,
                                                         PermanentPredicate permanentFilter,
                                                         Set<Keyword> keywords,
                                                         EffectSlot triggeredAbilitySlot,
                                                         CardEffect triggeredAbility) {
        this(handFilter, permanentFilter, keywords, triggeredAbilitySlot, triggeredAbility, null, null);
    }

    @Override
    public GrantScope scope() {
        return GrantScope.OWN_CREATURES;
    }

    @Override
    public PermanentPredicate filter() {
        return permanentFilter;
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
