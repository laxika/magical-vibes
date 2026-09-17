package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.carddata.DeckLegalityRegistry;
import com.github.laxika.magicalvibes.model.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class DeckValidationService {
    private final DeckLegalityRegistry legalities;
    public DeckValidationService(DeckLegalityRegistry legalities) { this.legalities = legalities; }

    public boolean eligibleCommander(Card card) {
        return ((card.hasType(CardType.CREATURE) || card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(com.github.laxika.magicalvibes.model.effect.BecomeCreatureOutsideBattlefieldEffect.class::isInstance))
                && card.getSupertypes().contains(CardSupertype.LEGENDARY))
                || text(card).contains("can be your commander");
    }

    public DeckValidation validate(DeckDefinition deck, DeckFormat format) {
        List<String> errors = new ArrayList<>();
        List<Card> all = new ArrayList<>(deck.mainDeck());
        all.addAll(deck.sideboard());
        if (deck.commander() != null) all.add(deck.commander());
        if (deck.mainDeck().size() < 7) errors.add("The main deck needs at least seven cards to draw an opening hand.");
        if (format == DeckFormat.COMMANDER) {
            if (deck.commander() == null) errors.add("Select one commander.");
            else if (!eligibleCommander(deck.commander())) errors.add("The selected card cannot be your commander.");
            if (deck.mainDeck().size() != 99) errors.add("Commander requires 99 main-deck cards plus one commander.");
            if (!deck.sideboard().isEmpty()) errors.add("Commander sideboards are not supported.");
        } else {
            if (deck.commander() != null) errors.add("Only Commander decks may designate a commander.");
            if (format != DeckFormat.CASUAL && deck.mainDeck().size() < 60) errors.add("The main deck must contain at least 60 cards.");
            if (format != DeckFormat.CASUAL && deck.sideboard().size() > 15) errors.add("The sideboard may contain at most 15 cards.");
        }
        Map<String, List<Card>> byName = new LinkedHashMap<>();
        all.forEach(card -> byName.computeIfAbsent(card.getName(), key -> new ArrayList<>()).add(card));
        java.time.Instant oldest = null;
        for (var entry : byName.entrySet()) {
            Card card = entry.getValue().getFirst();
            int limit = copyLimit(card, format == DeckFormat.COMMANDER ? 1 : 4);
            if (format != DeckFormat.CASUAL) {
                String legality = legalities.status(card, format);
                if ("restricted".equals(legality) && format == DeckFormat.VINTAGE) limit = 1;
                else if (!"legal".equals(legality)) errors.add(card.getName() + ": " + legality + " in " + format + ".");
                var date = legalities.snapshot(card).updatedAt();
                if (date != null && (oldest == null || date.isBefore(oldest))) oldest = date;
            }
            if (entry.getValue().size() > limit) errors.add(card.getName() + ": maximum " + limit + " copies across the deck and sideboard.");
            if (card.getType() != null && card.getType().isPlanar()) errors.add(card.getName() + ": planar cards belong in the planar deck.");
            if (format == DeckFormat.COMMANDER && deck.commander() != null) {
                Set<CardColor> colors = new HashSet<>(card.getColorIdentity());
                if (card.hasType(CardType.LAND)) {
                    for (var pair : Map.of(CardSubtype.PLAINS, CardColor.WHITE, CardSubtype.ISLAND, CardColor.BLUE,
                            CardSubtype.SWAMP, CardColor.BLACK, CardSubtype.MOUNTAIN, CardColor.RED, CardSubtype.FOREST, CardColor.GREEN).entrySet()) {
                        if (card.getSubtypes().contains(pair.getKey())) colors.add(pair.getValue());
                    }
                }
                if (!deck.commander().getColorIdentity().containsAll(colors)) errors.add(card.getName() + ": outside the commander's color identity.");
            }
        }
        return new DeckValidation(errors, oldest == null ? null : oldest.toString());
    }

    private static String text(Card card) { return Objects.toString(card.getCardText(), "").toLowerCase(Locale.ROOT); }
    private static int copyLimit(Card card, int normal) {
        if (card.getSupertypes().contains(CardSupertype.BASIC) || text(card).contains("a deck can have any number of cards named")) return Integer.MAX_VALUE;
        var match = Pattern.compile("a deck can have up to (\\w+) cards named").matcher(text(card));
        if (match.find()) {
            String number = match.group(1);
            try { return Integer.parseInt(number); } catch (NumberFormatException ignored) { }
            return Map.of("two", 2, "three", 3, "four", 4, "five", 5, "six", 6, "seven", 7, "eight", 8, "nine", 9).getOrDefault(number, normal);
        }
        return normal;
    }
}
