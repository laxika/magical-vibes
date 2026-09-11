package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Static effect for Eater of Virtue: the equipped creature gains watched keywords found on cards
 * exiled with the source Equipment, along with fixed protection abilities from those cards.
 */
public record GainKeywordsOfCardsExiledWithSourceToEquippedCreatureEffect(
        Set<Keyword> keywords
) implements KeywordGrantingEffect {

    private static final Set<Keyword> DEFAULT_KEYWORDS = Set.of(
            Keyword.FLYING,
            Keyword.FIRST_STRIKE,
            Keyword.DOUBLE_STRIKE,
            Keyword.DEATHTOUCH,
            Keyword.HASTE,
            Keyword.HEXPROOF,
            Keyword.INDESTRUCTIBLE,
            Keyword.LIFELINK,
            Keyword.MENACE,
            Keyword.REACH,
            Keyword.TRAMPLE,
            Keyword.VIGILANCE
    );

    public GainKeywordsOfCardsExiledWithSourceToEquippedCreatureEffect() {
        this(DEFAULT_KEYWORDS);
    }

    public GainKeywordsOfCardsExiledWithSourceToEquippedCreatureEffect {
        keywords = Set.copyOf(keywords);
    }

    @Override
    public GrantScope scope() {
        return GrantScope.EQUIPPED_CREATURE;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }
}
