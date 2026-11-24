package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.RemoveCountersFromControlledCreaturesCastingCost;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Static marker effect: "You may cast [types] from the top of your library."
 * While a permanent with this effect is on the battlefield, the controller may
 * cast spells of the specified types or matching the optional card predicate from the top of their
 * library (paying their mana cost normally). The optional colorless clause is separate from the
 * type clause because colorless is a characteristic, not a card type. The optional once-per-turn
 * flag limits that source to one normal-cost top-library cast each turn. The granted keywords are
 * given until end of turn to a spell cast through this permission ("If you cast a creature spell this
 * way, it gains haste until end of turn"); a spell cast through another permission does not get them.
 *
 * <p>An {@code alternativeCost} is what a spell cast this way is paid with rather than its mana cost ("If you cast a
 * spell this way, pay life equal to its mana value rather than pay its mana cost", CR 118.9); {@code null} pays the
 * mana cost. The permission is a static ability of a permanent, or a floating copy a resolution gave a player until end
 * of turn ({@link GrantStaticEffectToPlayerEffect}): "until end of turn, you may play cards from the top of your
 * library" is that grant over this record and {@link PlayLandsFromTopOfLibraryEffect}.
 */
public record AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes, boolean castableColorless,
                                              CardPredicate filter, boolean oncePerTurn,
                                              List<CastingCost> additionalCosts, int enterWithCounterCount,
                                              boolean grantsFlash, Set<Keyword> grantedKeywords,
                                              AlternativeSpellCost alternativeCost)
        implements CardEffect {

    /** Every card type a spell can have: "you may cast spells from the top of your library". */
    public static final Set<CardType> SPELL_TYPES = Set.of(CardType.CREATURE, CardType.ENCHANTMENT, CardType.SORCERY,
            CardType.INSTANT, CardType.ARTIFACT, CardType.PLANESWALKER, CardType.BATTLE, CardType.KINDRED);

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes, boolean castableColorless,
                                           CardPredicate filter, boolean oncePerTurn,
                                           List<CastingCost> additionalCosts, int enterWithCounterCount,
                                           boolean grantsFlash, Set<Keyword> grantedKeywords) {
        this(castableTypes, castableColorless, filter, oncePerTurn, additionalCosts, enterWithCounterCount,
                grantsFlash, grantedKeywords, null);
    }

    /** "You may cast spells from the top of your library", paying their mana costs. */
    public static AllowCastFromTopOfLibraryEffect anySpell() {
        return new AllowCastFromTopOfLibraryEffect(SPELL_TYPES);
    }

    /**
     * This permission with "If you cast a spell this way, pay life equal to its mana value rather than pay its mana
     * cost" (Bolas's Citadel, Gwenom, Remorseless): an alternative cost (CR 118.9), so no other alternative cost may be
     * used with it, while additional costs and cost increases are still paid.
     */
    public AllowCastFromTopOfLibraryEffect payingLifeEqualToManaValue() {
        return new AllowCastFromTopOfLibraryEffect(castableTypes, castableColorless, filter, oncePerTurn,
                additionalCosts, enterWithCounterCount, grantsFlash, grantedKeywords,
                new PayLifeEqualToSpellManaValueCost());
    }

    /** Whether a spell cast this way is paid with life equal to its mana value rather than its mana cost. */
    public boolean paysLifeEqualToManaValue() {
        return alternativeCost instanceof PayLifeEqualToSpellManaValueCost;
    }

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes, boolean castableColorless,
                                           CardPredicate filter, boolean oncePerTurn,
                                           List<CastingCost> additionalCosts, int enterWithCounterCount,
                                           boolean grantsFlash) {
        this(castableTypes, castableColorless, filter, oncePerTurn, additionalCosts,
                enterWithCounterCount, grantsFlash, Set.of());
    }

    /** A top-library permission whose spells gain the keywords until end of turn when cast this way. */
    public static AllowCastFromTopOfLibraryEffect grantingKeywords(CardPredicate filter, Set<Keyword> keywords) {
        return new AllowCastFromTopOfLibraryEffect(Set.of(), false, filter, false, List.of(), 0, false, keywords);
    }

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes, boolean castableColorless,
                                           CardPredicate filter, boolean oncePerTurn,
                                           List<CastingCost> additionalCosts) {
        this(castableTypes, castableColorless, filter, oncePerTurn, additionalCosts, 0, false);
    }

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes, boolean castableColorless,
                                           CardPredicate filter, boolean oncePerTurn,
                                           List<CastingCost> additionalCosts, int enterWithCounterCount) {
        this(castableTypes, castableColorless, filter, oncePerTurn, additionalCosts,
                enterWithCounterCount, false);
    }

    public AllowCastFromTopOfLibraryEffect {
        additionalCosts = additionalCosts == null ? List.of() : List.copyOf(additionalCosts);
        grantedKeywords = grantedKeywords == null ? Set.of() : Set.copyOf(grantedKeywords);
        if (alternativeCost != null && !(alternativeCost instanceof PayLifeEqualToSpellManaValueCost)) {
            throw new IllegalArgumentException("No library-top cast pays " + alternativeCost);
        }
    }

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes, boolean castableColorless,
                                           CardPredicate filter, boolean oncePerTurn) {
        this(castableTypes, castableColorless, filter, oncePerTurn, List.of());
    }

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes) {
        this(castableTypes, false, null, false, List.of());
    }

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes, boolean castableColorless) {
        this(castableTypes, castableColorless, null, false, List.of());
    }

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes,
                                           List<? extends CastingCost> additionalCosts) {
        this(castableTypes, false, null, false, List.copyOf(additionalCosts));
    }

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes, boolean castableColorless,
                                           CardPredicate filter) {
        this(castableTypes, castableColorless, filter, false);
    }

    public AllowCastFromTopOfLibraryEffect(CardPredicate filter) {
        this(Set.of(), false, filter, false);
    }

    /** A normal-cost permission to cast matching cards from the top once each turn per source. */
    public static AllowCastFromTopOfLibraryEffect onceEachTurn(Set<CardType> castableTypes) {
        return new AllowCastFromTopOfLibraryEffect(castableTypes, false, null, true);
    }

    public AllowCastFromTopOfLibraryEffect(CardPredicate filter, boolean oncePerTurn) {
        this(Set.of(), false, filter, oncePerTurn);
    }

    /** A top-library permission that also lets matching spells be cast as though they had flash. */
    public static AllowCastFromTopOfLibraryEffect withFlash(CardPredicate filter) {
        return new AllowCastFromTopOfLibraryEffect(Set.of(), false, filter, false, List.of(), 0, true);
    }

    public boolean matches(Card card) {
        if (card.getType() == CardType.LAND) return false;
        boolean matchesType = castableTypes.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(castableTypes::contains);
        boolean matchesColorless = castableColorless
                && (card.getColors() == null || card.getColors().isEmpty());
        return matchesType || matchesColorless;
    }

    public OptionalInt counterRemovalCost() {
        if (additionalCosts.size() != 1
                || !(additionalCosts.getFirst() instanceof RemoveCountersFromControlledCreaturesCastingCost cost)) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(cost.count());
    }
}
