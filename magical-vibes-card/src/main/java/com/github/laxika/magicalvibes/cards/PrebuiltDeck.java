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
            new DeckEntry("311", 1),   // Angel's Feather
            new DeckEntry("326", 1)    // Icy Manipulator
    )),

    ARCANISS_GUILE("arcaniss-guile", "Arcanis's Guile (Tenth Edition Theme Deck)", CardSet.TENTH_EDITION, List.of(
            new DeckEntry("368", 17),  // Island
            new DeckEntry("104", 2),   // Sage Owl
            new DeckEntry("74", 2),    // Cloud Elemental
            new DeckEntry("96", 1),    // Phantom Warrior
            new DeckEntry("68", 1),    // Aven Fisher
            new DeckEntry("115", 1),   // Thieving Magpie
            new DeckEntry("64", 1),    // Air Elemental
            new DeckEntry("66", 1),    // Arcanis the Omnipotent
            new DeckEntry("80", 1),    // Denizen of the Deep
            new DeckEntry("122", 2),   // Unsummon
            new DeckEntry("100", 2),   // Remove Soul
            new DeckEntry("114", 1),   // Telling Time
            new DeckEntry("70", 1),    // Boomerang
            new DeckEntry("76", 2),    // Counsel of the Soratami
            new DeckEntry("71", 2),    // Cancel
            new DeckEntry("116", 1),   // Tidings
            new DeckEntry("329", 1),   // Kraken's Eye
            new DeckEntry("341", 1)    // Rod of Ruin
    )),

    KAMAHLS_TEMPER("kamahls-temper", "Kamahl's Temper (Tenth Edition Theme Deck)", CardSet.TENTH_EDITION, List.of(
            new DeckEntry("376", 17),  // Mountain
            new DeckEntry("224", 1),   // Raging Goblin
            new DeckEntry("246", 1),   // Viashino Sandscout
            new DeckEntry("192", 2),   // Bloodrock Cyclops
            new DeckEntry("193", 2),   // Bogardan Firefiend
            new DeckEntry("221", 1),   // Prodigal Pyromancer
            new DeckEntry("217", 2),   // Lightning Elemental
            new DeckEntry("205", 1),   // Furnace Whelp
            new DeckEntry("243", 2),   // Thundering Giant
            new DeckEntry("214", 1),   // Kamahl, Pit Fighter
            new DeckEntry("232", 1),   // Shock
            new DeckEntry("213", 2),   // Incinerate
            new DeckEntry("189", 1),   // Beacon of Destruction
            new DeckEntry("190", 1),   // Blaze
            new DeckEntry("238", 2),   // Spitting Earth
            new DeckEntry("242", 1),   // Threaten
            new DeckEntry("322", 1),   // Dragon's Claw
            new DeckEntry("337", 1)    // Phyrexian Vault
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
