package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.AbilityEffects;
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

    public PerpetuallyGrantKeywordToMatchingCardsEffect {
        keywords = Set.copyOf(keywords);
    }

    public PerpetuallyGrantKeywordToMatchingCardsEffect(CardPredicate handFilter,
                                                         PermanentPredicate permanentFilter,
                                                         Set<Keyword> keywords) {
        this(handFilter, permanentFilter, keywords, null, null, null);
    }

    /** Keywords and {@code grantedAbility} for matching cards in hand and matching permanents. */
    public PerpetuallyGrantKeywordToMatchingCardsEffect(CardPredicate handFilter,
                                                         PermanentPredicate permanentFilter,
                                                         Set<Keyword> keywords,
                                                         Ability grantedAbility) {
        this(handFilter, permanentFilter, keywords, grantedAbility, null, null);
    }

    @Override
    public PermanentSubject subject() {
        return Subjects.otherCreaturesYouControl();
    }

    @Override
    public PermanentPredicate filter() {
        return permanentFilter;
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredAbility() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
