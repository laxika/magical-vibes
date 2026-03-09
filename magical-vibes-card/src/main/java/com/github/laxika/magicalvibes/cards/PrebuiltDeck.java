package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.model.Card;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum PrebuiltDeck {

    CHO_MANNOS_RESOLVE("cho-mannos-resolve", "Cho-Manno's Resolve (Tenth Edition Theme Deck)", List.of(
            new DeckEntry(CardSet.TENTH_EDITION, "364", 17),  // Plains
            new DeckEntry(CardSet.TENTH_EDITION, "16", 1),    // Ghost Warden
            new DeckEntry(CardSet.TENTH_EDITION, "62", 2),    // Youthful Knight
            new DeckEntry(CardSet.TENTH_EDITION, "11", 2),    // Benalish Knight
            new DeckEntry(CardSet.TENTH_EDITION, "55", 1),    // Venerable Monk
            new DeckEntry(CardSet.TENTH_EDITION, "59", 2),    // Wild Griffin
            new DeckEntry(CardSet.TENTH_EDITION, "12", 1),    // Cho-Manno, Revolutionary
            new DeckEntry(CardSet.TENTH_EDITION, "41", 2),    // Skyhunter Patrol
            new DeckEntry(CardSet.TENTH_EDITION, "2", 2),     // Angel of Mercy
            new DeckEntry(CardSet.TENTH_EDITION, "26", 2),    // Loxodon Mystic
            new DeckEntry(CardSet.TENTH_EDITION, "1", 1),     // Ancestor's Chosen
            new DeckEntry(CardSet.TENTH_EDITION, "13", 1),    // Condemn
            new DeckEntry(CardSet.TENTH_EDITION, "31", 2),    // Pacifism
            new DeckEntry(CardSet.TENTH_EDITION, "33", 1),    // Pariah
            new DeckEntry(CardSet.TENTH_EDITION, "40", 1),    // Serra's Embrace
            new DeckEntry(CardSet.TENTH_EDITION, "311", 1),   // Angel's Feather
            new DeckEntry(CardSet.TENTH_EDITION, "326", 1)    // Icy Manipulator
    )),

    ARCANISS_GUILE("arcaniss-guile", "Arcanis's Guile (Tenth Edition Theme Deck)", List.of(
            new DeckEntry(CardSet.TENTH_EDITION, "368", 17),  // Island
            new DeckEntry(CardSet.TENTH_EDITION, "104", 2),   // Sage Owl
            new DeckEntry(CardSet.TENTH_EDITION, "74", 2),    // Cloud Elemental
            new DeckEntry(CardSet.TENTH_EDITION, "96", 1),    // Phantom Warrior
            new DeckEntry(CardSet.TENTH_EDITION, "68", 1),    // Aven Fisher
            new DeckEntry(CardSet.TENTH_EDITION, "115", 1),   // Thieving Magpie
            new DeckEntry(CardSet.TENTH_EDITION, "64", 1),    // Air Elemental
            new DeckEntry(CardSet.TENTH_EDITION, "66", 1),    // Arcanis the Omnipotent
            new DeckEntry(CardSet.TENTH_EDITION, "80", 1),    // Denizen of the Deep
            new DeckEntry(CardSet.TENTH_EDITION, "122", 2),   // Unsummon
            new DeckEntry(CardSet.TENTH_EDITION, "100", 2),   // Remove Soul
            new DeckEntry(CardSet.TENTH_EDITION, "114", 1),   // Telling Time
            new DeckEntry(CardSet.TENTH_EDITION, "70", 1),    // Boomerang
            new DeckEntry(CardSet.TENTH_EDITION, "76", 2),    // Counsel of the Soratami
            new DeckEntry(CardSet.TENTH_EDITION, "71", 2),    // Cancel
            new DeckEntry(CardSet.TENTH_EDITION, "116", 1),   // Tidings
            new DeckEntry(CardSet.TENTH_EDITION, "329", 1),   // Kraken's Eye
            new DeckEntry(CardSet.TENTH_EDITION, "341", 1)    // Rod of Ruin
    )),

    KAMAHLS_TEMPER("kamahls-temper", "Kamahl's Temper (Tenth Edition Theme Deck)", List.of(
            new DeckEntry(CardSet.TENTH_EDITION, "376", 17),  // Mountain
            new DeckEntry(CardSet.TENTH_EDITION, "224", 1),   // Raging Goblin
            new DeckEntry(CardSet.TENTH_EDITION, "246", 1),   // Viashino Sandscout
            new DeckEntry(CardSet.TENTH_EDITION, "192", 2),   // Bloodrock Cyclops
            new DeckEntry(CardSet.TENTH_EDITION, "193", 2),   // Bogardan Firefiend
            new DeckEntry(CardSet.TENTH_EDITION, "221", 1),   // Prodigal Pyromancer
            new DeckEntry(CardSet.TENTH_EDITION, "217", 2),   // Lightning Elemental
            new DeckEntry(CardSet.TENTH_EDITION, "205", 1),   // Furnace Whelp
            new DeckEntry(CardSet.TENTH_EDITION, "243", 2),   // Thundering Giant
            new DeckEntry(CardSet.TENTH_EDITION, "214", 1),   // Kamahl, Pit Fighter
            new DeckEntry(CardSet.TENTH_EDITION, "232", 1),   // Shock
            new DeckEntry(CardSet.TENTH_EDITION, "213", 2),   // Incinerate
            new DeckEntry(CardSet.TENTH_EDITION, "189", 1),   // Beacon of Destruction
            new DeckEntry(CardSet.TENTH_EDITION, "190", 1),   // Blaze
            new DeckEntry(CardSet.TENTH_EDITION, "238", 2),   // Spitting Earth
            new DeckEntry(CardSet.TENTH_EDITION, "242", 1),   // Threaten
            new DeckEntry(CardSet.TENTH_EDITION, "322", 1),   // Dragon's Claw
            new DeckEntry(CardSet.TENTH_EDITION, "337", 1)    // Phyrexian Vault
    )),

    DEADSPREAD("deadspread", "Deadspread (Scars of Mirrodin Intro Pack)", List.of(
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "234", 13),  // Island
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "238", 13),  // Swamp
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "57", 1),    // Carnifex Demon
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "30", 1),    // Darkslick Drake
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "63", 2),    // Fume Spitter
            new DeckEntry(CardSet.MAGIC_2011, "56", 1),            // Harbor Serpent
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "170", 2),   // Leaden Myr
            new DeckEntry(CardSet.MAGIC_2011, "63", 2),            // Maritime Guard
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "70", 2),    // Moriok Reaver
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "202", 2),   // Silver Myr
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "78", 1),    // Skinrender
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "44", 2),    // Sky-Eel School
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "47", 2),    // Thrummingbird
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "144", 2),   // Contagion Clasp
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "145", 1),   // Contagion Engine
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "148", 1),   // Culling Dais
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "184", 2),   // Necrogen Censer
            new DeckEntry(CardSet.MAGIC_2011, "213", 1),           // Sorcerer's Strongbox
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "211", 1),   // Throne of Geth
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "213", 1),   // Trigon of Corruption
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "217", 1),   // Trigon of Thought
            new DeckEntry(CardSet.MAGIC_2011, "95", 1),            // Doom Blade
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "68", 2),    // Instill Infection
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "45", 2),    // Steady Progress
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "50", 1)     // Twisted Image
    )),

    METALCRAFT("metalcraft", "Metalcraft (Scars of Mirrodin Intro Pack)", List.of(
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "234", 12),  // Island
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "242", 12),  // Mountain
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "28", 1),    // Argent Sphinx
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "84", 2),    // Blade-Tribe Berserkers
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "142", 3),   // Chrome Steed
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "87", 2),    // Embersmith
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "154", 1),   // Etched Champion
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "168", 2),   // Iron Myr
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "36", 2),    // Lumengrid Drake
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "174", 2),   // Memnite
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "40", 2),    // Riddlesmith
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "202", 2),   // Silver Myr
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "203", 3),   // Snapsail Glider
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "48", 2),    // Trinket Mage
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "136", 1),   // Accorder's Shield
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "149", 1),   // Darksteel Axe
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "153", 1),   // Echo Circlet
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "160", 1),   // Golem Foundry
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "161", 1),   // Golem's Heart
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "199", 2),   // Rusted Relic
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "209", 1),   // Sylvok Lifestaff
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "31", 1),    // Disperse
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "91", 3)     // Galvanic Blast
    )),

    MYR_OF_MIRRODIN("myr-of-mirrodin", "Myr of Mirrodin (Scars of Mirrodin Intro Pack)", List.of(
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "230", 24),  // Plains
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "3", 1),     // Auriok Edgewright
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "151", 2),   // Darksteel Myr
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "152", 1),   // Darksteel Sentinel
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "8", 2),     // Ghalma's Warden
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "157", 3),   // Gold Myr
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "13", 2),    // Kemba's Skyguard
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "180", 1),   // Myr Battlesphere
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "181", 2),   // Myr Galvanizer
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "16", 2),    // Myrsmith
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "190", 2),   // Palladium Myr
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "192", 2),   // Perilous Myr
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "17", 1),    // Razor Hippogriff
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "22", 1),    // Sunblast Angel
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "189", 3),   // Origin Spellbomb
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "2", 3),     // Arrest
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "5", 1),     // Dispense Justice
            // new DeckEntry(CardSet.MAGIC_2011, "19", 2),         // Inspired Charge (not yet implemented)
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "20", 2),    // Seize the Initiative
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "18", 3)     // Revoke Existence
    )),

    EVINCARS_TYRANNY("evincars-tyranny", "Evincar's Tyranny (Tenth Edition Theme Deck)", List.of(
            new DeckEntry(CardSet.TENTH_EDITION, "372", 17),  // Swamp
            new DeckEntry(CardSet.TENTH_EDITION, "186", 2),   // Vampire Bats
            new DeckEntry(CardSet.TENTH_EDITION, "139", 1),   // Drudge Skeletons
            new DeckEntry(CardSet.TENTH_EDITION, "183", 1),   // Thrull Surgeon
            new DeckEntry(CardSet.TENTH_EDITION, "153", 1),   // Looming Shade
            new DeckEntry(CardSet.TENTH_EDITION, "177", 2),   // Severed Legion
            new DeckEntry(CardSet.TENTH_EDITION, "138", 1),   // Dross Crocodile
            new DeckEntry(CardSet.TENTH_EDITION, "146", 1),   // Gravedigger
            new DeckEntry(CardSet.TENTH_EDITION, "161", 1),   // Mortivore
            new DeckEntry(CardSet.TENTH_EDITION, "156", 1),   // Mass of Ghouls
            new DeckEntry(CardSet.TENTH_EDITION, "127", 1),   // Ascendant Evincar
            new DeckEntry(CardSet.TENTH_EDITION, "185", 1),   // Unholy Strength
            new DeckEntry(CardSet.TENTH_EDITION, "182", 1),   // Terror
            new DeckEntry(CardSet.TENTH_EDITION, "133", 1),   // Cruel Edict
            new DeckEntry(CardSet.TENTH_EDITION, "135", 1),   // Diabolic Tutor
            new DeckEntry(CardSet.TENTH_EDITION, "141", 1),   // Essence Drain
            new DeckEntry(CardSet.TENTH_EDITION, "131", 1),   // Consume Spirit
            new DeckEntry(CardSet.TENTH_EDITION, "128", 1),   // Assassinate
            new DeckEntry(CardSet.TENTH_EDITION, "159", 2),   // Mind Rot
            new DeckEntry(CardSet.TENTH_EDITION, "312", 1),   // Bottle Gnomes
            new DeckEntry(CardSet.TENTH_EDITION, "320", 1)    // Demon's Horn
    ));

    public record DeckEntry(CardSet cardSet, String collectorNumber, int count) {}

    private final String id;
    private final String name;
    private final List<DeckEntry> entries;

    public List<Card> buildDeck() {
        List<Card> deck = new ArrayList<>();
        for (DeckEntry entry : entries) {
            CardPrinting printing = entry.cardSet().findByCollectorNumber(entry.collectorNumber());
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
