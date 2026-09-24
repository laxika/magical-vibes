package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/** Perpetually grants keywords to matching cards in hand and matching permanents on the battlefield. */
public record PerpetuallyGrantKeywordToMatchingCardsEffect(
        CardPredicate handFilter,
        PermanentPredicate permanentFilter,
        Set<Keyword> keywords,
        EffectSlot triggeredAbilitySlot,
        CardEffect triggeredAbility) implements KeywordGrantingEffect {

    public PerpetuallyGrantKeywordToMatchingCardsEffect(CardPredicate handFilter,
                                                         PermanentPredicate permanentFilter,
                                                         Set<Keyword> keywords) {
        this(handFilter, permanentFilter, keywords, null, null);
    }

    public PerpetuallyGrantKeywordToMatchingCardsEffect {
        keywords = Set.copyOf(keywords);
        if ((triggeredAbilitySlot == null) != (triggeredAbility == null)) {
            throw new IllegalArgumentException("Triggered ability slot and effect must be provided together");
        }
    }

    @Override
    public GrantScope scope() {
        return GrantScope.OWN_CREATURES;
    }

    @Override
    public PermanentPredicate filter() {
        return permanentFilter;
    }
}
