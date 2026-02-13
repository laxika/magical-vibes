package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.model.Card;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum PrebuiltDeck {

    CHO_MANNOS_RESOLVE("cho-mannos-resolve", "Cho-Manno's Resolve (Tenth Edition Theme Deck)", CardSet.TENTH_EDITION, List.of(
            new DeckEntry("364", 17),  // Plains
            new DeckEntry("16", 1),    // Ghost Warden
            new DeckEntry("62", 2),    // Youthful Knight
            new DeckEntry("11", 2),    // Benalish Knight
            new DeckEntry("55", 1),    // Venerable Monk
            new DeckEntry("59", 2),    // Wild Griffin
            new DeckEntry("12", 1),    // Cho-Manno, Revolutionary
            new DeckEntry("41", 2),    // Skyhunter Patrol
            new DeckEntry("2", 2),     // Angel of Mercy
            new DeckEntry("26", 2),    // Loxodon Mystic
            new DeckEntry("1", 1),     // Ancestor's Chosen
            new DeckEntry("13", 1),    // Condemn
            new DeckEntry("31", 2),    // Pacifism
            new DeckEntry("33", 1),    // Pariah
            new DeckEntry("40", 1),    // Serra's Embrace
            new DeckEntry("311", 1)    // Angel's Feather
    ));

    public record DeckEntry(String collectorNumber, int count) {}

    private final String id;
    private final String name;
    private final CardSet cardSet;
    private final List<DeckEntry> entries;

    public List<Card> buildDeck() {
        List<Card> deck = new ArrayList<>();
        for (DeckEntry entry : entries) {
            CardPrinting printing = cardSet.findByCollectorNumber(entry.collectorNumber());
            for (int i = 0; i < entry.count(); i++) {
                deck.add(printing.createCard());
            }
        }
        return deck;
    }

    public static PrebuiltDeck findById(String id) {
        for (PrebuiltDeck deck : values()) {
            if (deck.id.equals(id)) {
                return deck;
            }
        }
        throw new IllegalArgumentException("No prebuilt deck with id: " + id);
    }
}
