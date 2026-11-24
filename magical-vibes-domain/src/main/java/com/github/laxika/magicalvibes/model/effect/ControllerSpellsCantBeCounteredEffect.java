package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;
import java.util.Set;

/**
 * Static: "[players'] [matching] spells can't be countered." {@code players} names whose spells, judged by the spell's
 * controller ({@link Subjects#you()}, the default: "spells you control"; {@link Subjects#eachPlayer()}: "creature
 * spells can't be countered", Lier, Root Sliver). The spell must also match every restriction given: not a creature
 * ({@code noncreatureOnly}), a mana value of at least {@code minimumManaValue} (X included on the stack), one of
 * {@code cardTypes}, {@code predicate}, and with {@code castOnly} a spell that was cast (a copy isn't). Read by
 * {@code GameQueryService.isUncounterable} through the permanent's active static abilities, so a face-down permanent
 * or one that lost its abilities protects nothing (CR 708.2, 613.1f); a face-down spell is judged by its face-down
 * characteristics.
 */
public record ControllerSpellsCantBeCounteredEffect(PlayerSubject players,
                                                    boolean noncreatureOnly,
                                                    Integer minimumManaValue,
                                                    Set<CardType> cardTypes,
                                                    CardPredicate predicate, boolean castOnly) implements CardEffect {

    public ControllerSpellsCantBeCounteredEffect {
        Objects.requireNonNull(players, "players");
        cardTypes = cardTypes == null ? Set.of() : Set.copyOf(cardTypes);
    }

    public ControllerSpellsCantBeCounteredEffect(boolean noncreatureOnly, Integer minimumManaValue,
                                                Set<CardType> cardTypes, CardPredicate predicate, boolean castOnly) {
        this(Subjects.you(), noncreatureOnly, minimumManaValue, cardTypes, predicate, castOnly);
    }

    public ControllerSpellsCantBeCounteredEffect(boolean noncreatureOnly, Integer minimumManaValue,
                                                Set<CardType> cardTypes, CardPredicate predicate) {
        this(noncreatureOnly, minimumManaValue, cardTypes, predicate, false);
    }

    /** "[players'] spells of {@code cardTypes} (any, when empty) matching {@code predicate} can't be countered." */
    public ControllerSpellsCantBeCounteredEffect(PlayerSubject players, Set<CardType> cardTypes,
                                                CardPredicate predicate) {
        this(players, false, null, cardTypes, predicate, false);
    }

    public ControllerSpellsCantBeCounteredEffect() {
        this(false, null, Set.of(), null);
    }

    public ControllerSpellsCantBeCounteredEffect(boolean noncreatureOnly) {
        this(noncreatureOnly, null, Set.of(), null);
    }

    public ControllerSpellsCantBeCounteredEffect(boolean noncreatureOnly, Integer minimumManaValue) {
        this(noncreatureOnly, minimumManaValue, Set.of(), null);
    }

    public ControllerSpellsCantBeCounteredEffect(int minimumManaValue) {
        this(false, minimumManaValue, Set.of(), null);
    }

    public ControllerSpellsCantBeCounteredEffect(Set<CardType> cardTypes) {
        this(false, null, cardTypes, null);
    }

    public ControllerSpellsCantBeCounteredEffect(CardPredicate predicate, boolean castOnly) {
        this(false, null, Set.of(), predicate, castOnly);
    }

    public ControllerSpellsCantBeCounteredEffect(CardPredicate predicate) {
        this(false, null, Set.of(), predicate);
    }
}
