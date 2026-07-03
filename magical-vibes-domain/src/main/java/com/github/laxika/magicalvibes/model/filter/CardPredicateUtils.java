package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CardPredicateUtils {

    private CardPredicateUtils() {
    }

    /**
     * A predicate matching basic land cards (a land card with the Basic supertype), composed from
     * existing predicates so no new dispatch handling is required in {@code PredicateEvaluationService}
     * or {@link #describeFilter}. Describes as "basic land card".
     */
    public static CardPredicate basicLand() {
        return new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.BASIC),
                new CardTypePredicate(CardType.LAND)));
    }

    public static String describeFilter(CardPredicate predicate) {
        if (predicate == null) return "card";
        if (predicate instanceof CardTypePredicate p) {
            return p.cardType().name().toLowerCase() + " card";
        }
        if (predicate instanceof CardSubtypePredicate p) {
            return p.subtype().getDisplayName() + " card";
        }
        if (predicate instanceof CardKeywordPredicate p) {
            return "card with " + p.keyword().name().toLowerCase().replace('_', ' ');
        }
        if (predicate instanceof CardIsAuraPredicate) {
            return "Aura card";
        }
        if (predicate instanceof CardIsPermanentPredicate) {
            return "permanent card";
        }
        if (predicate instanceof CardHasFlashbackPredicate) {
            return "card with flashback";
        }
        if (predicate instanceof CardIsHistoricPredicate) {
            return "historic card";
        }
        if (predicate instanceof CardSupertypePredicate p) {
            return p.supertype().getDisplayName().toLowerCase();
        }
        if (predicate instanceof CardMaxManaValuePredicate p) {
            return "card with mana value " + p.maxManaValue() + " or less";
        }
        if (predicate instanceof CardMinManaValuePredicate p) {
            return "card with mana value " + p.minManaValue() + " or greater";
        }
        if (predicate instanceof CardNamedPredicate p) {
            return "card named " + p.cardName();
        }
        if (predicate instanceof CardNotPredicate p) {
            String inner = describeFilter(p.predicate());
            if (inner.endsWith(" card")) {
                return "non-" + inner;
            }
            return "not " + inner;
        }
        if (predicate instanceof CardAllOfPredicate p) {
            // Render supertype adjectives ("basic", "legendary", "snow") first so a composed
            // predicate reads naturally, e.g. CardAllOf(LAND, BASIC) → "basic land card".
            List<CardPredicate> ordered = new ArrayList<>(p.predicates());
            ordered.sort(Comparator.comparingInt(sub -> sub instanceof CardSupertypePredicate ? 0 : 1));
            List<String> parts = new ArrayList<>();
            for (CardPredicate sub : ordered) {
                parts.add(describeFilter(sub));
            }
            // "creature card" + "card with infect" → "creature card with infect"
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.size(); i++) {
                String part = parts.get(i);
                if (i > 0 && part.startsWith("card with ")) {
                    sb.append(" with ").append(part.substring("card with ".length()));
                } else if (i > 0) {
                    sb.append(" ").append(part);
                } else {
                    sb.append(part);
                }
            }
            return sb.toString();
        }
        if (predicate instanceof CardAnyOfPredicate p) {
            List<String> parts = new ArrayList<>();
            for (CardPredicate sub : p.predicates()) {
                parts.add(describeFilter(sub));
            }
            // "artifact card or creature card" → "artifact or creature card"
            if (parts.size() >= 2 && parts.stream().allMatch(part -> part.endsWith(" card"))) {
                List<String> stripped = parts.stream()
                        .map(part -> part.substring(0, part.length() - " card".length()))
                        .toList();
                return String.join(" or ", stripped) + " card";
            }
            return String.join(" or ", parts);
        }
        return "card";
    }
}
