package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.ExileTopCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Static effect declaring that a card has splice, an optional additional cost paid while casting
 * a spell that meets the quality requirement (CR 702.47).
 *
 * <p>Costs are represented as the same composable casting-cost components used by alternate
 * casting options. The compatibility constructors and accessors keep existing tap, return, and
 * graveyard-exile splice cards on the same representation as newer splice cards with sacrifice
 * costs and splice-only effects.
 *
 * @param host the quality the host spell must have
 * @param costs the splice cost components
 * @param splicedEffects additional effects to add only when this card is spliced; the card's
 *                      SPELL effects are included as well
 */
public record SpliceEffect(SpliceHost host, List<CastingCost> costs,
                           List<CardEffect> splicedEffects) implements CardEffect {

    public SpliceEffect {
        Objects.requireNonNull(host);
        costs = List.copyOf(Objects.requireNonNull(costs));
        splicedEffects = List.copyOf(Objects.requireNonNull(splicedEffects));
    }

    public SpliceEffect(CardSubtype ontoSubtype, List<CastingCost> costs, List<CardEffect> splicedEffects) {
        this(SpliceHost.subtype(ontoSubtype), costs, splicedEffects);
    }

    public SpliceEffect(CardSubtype ontoSubtype, List<CastingCost> costs) {
        this(SpliceHost.subtype(ontoSubtype), costs, List.of());
    }

    public SpliceEffect(CardSubtype ontoSubtype, String manaCost) {
        this(SpliceHost.subtype(ontoSubtype), manaCosts(manaCost), List.of());
    }

    /** Compatibility constructor for "tap an untapped permanent" splice costs. */
    public SpliceEffect(CardSubtype ontoSubtype, String manaCost, PermanentPredicate tapCost) {
        this(SpliceHost.subtype(ontoSubtype), costsWithMana(manaCost, new TapUntappedPermanentsCost(1, tapCost)), List.of());
    }

    /** Splice onto any instant or sorcery spell. */
    public static SpliceEffect ontoInstantOrSorcery(String manaCost) {
        return new SpliceEffect(SpliceHost.INSTANT_OR_SORCERY, manaCosts(manaCost), List.of());
    }

    /** Splice cost whose only component is returning a matching permanent you control to hand. */
    public static SpliceEffect returning(CardSubtype ontoSubtype, PermanentPredicate returnCost) {
        return new SpliceEffect(SpliceHost.subtype(ontoSubtype), List.of(new ReturnPermanentsCost(1, returnCost)), List.of());
    }

    /** Splice cost whose only component is exiling {@code count} cards from your graveyard. */
    public static SpliceEffect exilingGraveyard(CardSubtype ontoSubtype, int count) {
        return new SpliceEffect(SpliceHost.subtype(ontoSubtype),
                List.of(new ExileTopCardsFromGraveyardCastingCost(null, "a card", count)), List.of());
    }

    /** The subtype required by a subtype-based splice ability, or {@code null} otherwise. */
    public CardSubtype ontoSubtype() {
        return host.ontoSubtype();
    }

    /** Whether this splice ability can be used with the supplied host spell. */
    public boolean appliesTo(Card hostSpell) {
        return host.appliesTo(hostSpell);
    }

    /** The mana portion of this splice cost, or an empty string when it has no mana component. */
    public String cost() {
        return costs.stream()
                .filter(ManaCastingCost.class::isInstance)
                .map(ManaCastingCost.class::cast)
                .map(ManaCastingCost::manaCost)
                .collect(Collectors.joining());
    }

    /** The tap filter, or null when this splice cost does not tap a permanent. */
    public PermanentPredicate tapCost() {
        return costs.stream()
                .filter(TapUntappedPermanentsCost.class::isInstance)
                .map(TapUntappedPermanentsCost.class::cast)
                .map(TapUntappedPermanentsCost::filter)
                .findFirst()
                .orElse(null);
    }

    /** The return filter, or null when this splice cost does not return a permanent. */
    public PermanentPredicate returnCost() {
        return costs.stream()
                .filter(ReturnPermanentsCost.class::isInstance)
                .map(ReturnPermanentsCost.class::cast)
                .map(ReturnPermanentsCost::filter)
                .findFirst()
                .orElse(null);
    }

    /** The permanent-choice component of this splice cost, or null when it has none. */
    public PermanentPredicate permanentCost() {
        PermanentPredicate tap = tapCost();
        return tap != null ? tap : returnCost();
    }

    /** The number of cards exiled from the graveyard by this splice cost. */
    public int exileFromGraveyardCount() {
        return costs.stream()
                .filter(ExileTopCardsFromGraveyardCastingCost.class::isInstance)
                .mapToInt(cost -> ((ExileTopCardsFromGraveyardCastingCost) cost).count())
                .sum();
    }

    private static List<CastingCost> manaCosts(String manaCost) {
        return manaCost == null || manaCost.isBlank() ? List.of() : List.of(new ManaCastingCost(manaCost));
    }

    private static List<CastingCost> costsWithMana(String manaCost, CastingCost additionalCost) {
        List<CastingCost> costs = new ArrayList<>(manaCosts(manaCost));
        costs.add(additionalCost);
        return costs;
    }

    public record SpliceHost(CardSubtype ontoSubtype, boolean instantOrSorcery) {

        private static final SpliceHost INSTANT_OR_SORCERY = new SpliceHost(null, true);

        public SpliceHost {
            if ((ontoSubtype == null && !instantOrSorcery)
                    || (ontoSubtype != null && instantOrSorcery)) {
                throw new IllegalArgumentException("A splice host must be a subtype or instant/sorcery");
            }
        }

        private static SpliceHost subtype(CardSubtype ontoSubtype) {
            return new SpliceHost(Objects.requireNonNull(ontoSubtype), false);
        }

        private boolean appliesTo(Card hostSpell) {
            return instantOrSorcery
                    ? hostSpell.hasType(CardType.INSTANT) || hostSpell.hasType(CardType.SORCERY)
                    : hostSpell.getSubtypes().contains(ontoSubtype);
        }
    }
}
