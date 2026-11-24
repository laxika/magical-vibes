package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;
import java.util.Set;

/**
 * Static effect: "as long as [a card among cards] has flying, [recipient] has flying. The same is true for [the other
 * watched keywords]" (layer 6, CR 613.1f). The cards are any static {@link CardSubject}: the creature cards exiled with
 * the source (Death-Mask Duplicant), the exiled creature cards with blood counters (Rayami), the creature cards in all
 * graveyards (Cairn Wanderer), the cards exiled with the source's delve ability ({@link Subjects#exiledAsCost()},
 * Soulflayer). A watched keyword's variants count: with {@link Keyword#HEXPROOF} watched, a card's "hexproof from
 * [quality]" is gained too (CR 702.11d); with {@code copiesProtection}, each of the card's own protection abilities
 * (CR 702.16a). A card exiled face down lends nothing (CR 406.3a), nor does a graveyard card while graveyard cards have
 * lost all abilities; a face-down source or one that lost its abilities gives none of them (CR 708.2).
 *
 * @param cards            whose keywords
 * @param cardFilter       which of those cards lend them, judged as they are now ("a creature card with flying was
 *                         exiled with this creature's delve ability"); {@code null} for all of them
 * @param watchedKeywords  the keywords the text lists
 * @param copiesProtection whether "protection" is among them
 * @param recipient        who has them: {@link Subjects#source()}, or the creature an Equipment is attached to
 *                         ({@link Subjects#attachedCreature()}, Eater of Virtue)
 */
public record GainKeywordsOfCreatureCardsExiledWithSourceEffect(
        CardSubject cards,
        CardPredicate cardFilter,
        Set<Keyword> watchedKeywords,
        boolean copiesProtection,
        PermanentSubject recipient
) implements KeywordGrantingEffect {

    /**
     * "Flying, first strike, double strike, deathtouch, haste, hexproof, indestructible, lifelink, menace, reach,
     * trample, and vigilance", the list most of these cards print (protection is {@code copiesProtection}).
     */
    public static final Set<Keyword> COMMON_KEYWORDS = Set.of(
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

    public GainKeywordsOfCreatureCardsExiledWithSourceEffect {
        Objects.requireNonNull(cards, "cards");
        Objects.requireNonNull(recipient, "recipient");
        watchedKeywords = Set.copyOf(watchedKeywords);
    }

    /** The source has the watched keywords of {@code cards}. */
    public GainKeywordsOfCreatureCardsExiledWithSourceEffect(CardSubject cards, Set<Keyword> watchedKeywords,
                                                            boolean copiesProtection) {
        this(cards, null, watchedKeywords, copiesProtection, Subjects.source());
    }

    /** The source has the watched keywords of those of {@code cards} that match {@code cardFilter}. */
    public GainKeywordsOfCreatureCardsExiledWithSourceEffect(CardSubject cards, CardPredicate cardFilter,
                                                            Set<Keyword> watchedKeywords, boolean copiesProtection) {
        this(cards, cardFilter, watchedKeywords, copiesProtection, Subjects.source());
    }

    @Override
    public Set<Keyword> keywords() {
        return watchedKeywords;
    }

    @Override
    public PermanentSubject subject() {
        return recipient;
    }

    /** No narrowing of {@link #recipient()}: this is {@link KeywordGrantingEffect}'s permanent filter. */
    @Override
    public PermanentPredicate filter() {
        return null;
    }
}
