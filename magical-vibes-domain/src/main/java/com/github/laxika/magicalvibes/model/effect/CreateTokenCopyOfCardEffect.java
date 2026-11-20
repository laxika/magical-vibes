package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * Creates a token that's a copy of a card outside the battlefield, for each card {@link #cards()} names (or, without a
 * subject, of {@link #sourceCard()}: a card supplied directly by a completed card-choice interaction, or with a
 * {@code null} card the card that caused the resolving trigger). {@link #tokenCopyEffect()} carries the copy's
 * exceptions and riders ("except it's 1/1", "it gains haste", "exile it at the beginning of the next end step",
 * "tapped and attacking") and how many copies of each card are made.
 *
 * <p>"Exile target creature card from a graveyard. Create a token that's a copy of it" is the exile step followed by
 * this effect over {@code Subjects.cardsThisWay(StepKind.EXILE)}: nothing is copied when nothing was exiled.
 * {@code Subjects.targetCard(filter, scope)} copies the targeted graveyard card where it is ("a copy of target creature
 * card in your graveyard", Feldon of the Third Path); only then does this effect declare a target.
 *
 * @param sourceCard           the card to copy when {@code cards} is {@code null}; {@code null} for the triggering card
 * @param tokenCopyEffect      the copy's exceptions and riders, and the number of copies of each card
 * @param cards                the cards to copy, or {@code null} for {@code sourceCard}
 * @param creatureTypesInstead "except it's a [creature types]" without "in addition to its other types" (CR 707.9b): the
 *                             copy's creature types are replaced by these; empty to keep them
 */
public record CreateTokenCopyOfCardEffect(
        Card sourceCard,
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect,
        CardSubject cards,
        List<CardSubtype> creatureTypesInstead
) implements CardEffect {

    public CreateTokenCopyOfCardEffect {
        creatureTypesInstead = creatureTypesInstead == null ? List.of() : List.copyOf(creatureTypesInstead);
        if (cards != null && sourceCard != null) {
            throw new IllegalArgumentException("A token copy copies a fixed card or a card subject, not both");
        }
    }

    /** Creates a token copy of {@code sourceCard}, or of the triggering card when it is {@code null}. */
    public CreateTokenCopyOfCardEffect(Card sourceCard, CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect) {
        this(sourceCard, tokenCopyEffect, null, List.of());
    }

    /** Creates a token copy of the card that caused the resolving trigger. */
    public static CreateTokenCopyOfCardEffect fromTriggeringCard(
            CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect) {
        return new CreateTokenCopyOfCardEffect(null, tokenCopyEffect);
    }

    /** "Create a token that's a copy of [it]": a copy of each card {@code cards} names, with {@code tokenCopyEffect}'s options. */
    public static CreateTokenCopyOfCardEffect of(CardSubject cards,
                                                 CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect) {
        return new CreateTokenCopyOfCardEffect(null, tokenCopyEffect, cards, List.of());
    }

    /** This copy "except it's a [creature types]": the copy's creature types are {@code creatureTypes} instead. */
    public CreateTokenCopyOfCardEffect withCreatureTypesInstead(List<CardSubtype> creatureTypes) {
        return new CreateTokenCopyOfCardEffect(sourceCard, tokenCopyEffect, cards, creatureTypes);
    }

    @Override
    public TargetSpec targetSpec() {
        if (cards instanceof CardSubject.TargetCard target) {
            return TargetSpec.benign(target.filter() == null
                    ? TargetPredicates.graveyardCard(target.scope())
                    : TargetPredicates.graveyardCards(target.filter(), target.scope()));
        }
        return TargetSpec.NONE;
    }
}
