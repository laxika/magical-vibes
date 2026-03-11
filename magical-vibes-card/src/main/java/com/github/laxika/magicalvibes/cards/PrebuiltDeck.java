package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.model.Card;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
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
            new DeckEntry(CardSet.MAGIC_2011, "19", 2),             // Inspired Charge
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "20", 2),    // Seize the Initiative
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "18", 3)     // Revoke Existence
    )),

    PHYREXIAN_POISON("phyrexian-poison", "Phyrexian Poison (Scars of Mirrodin Intro Pack)", List.of(
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "246", 13),  // Forest
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "238", 13),  // Swamp
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "54", 1),    // Blackcleave Goblin
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "58", 1),    // Contagious Nim
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "147", 2),   // Corpse Cur
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "117", 2),   // Cystbearer
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "66", 1),    // Hand of the Praetors
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "67", 2),    // Ichor Rats
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "166", 3),   // Ichorclaw Myr
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "185", 2),   // Necropede
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "75", 3),    // Plague Stinger
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "126", 1),   // Putrefax
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "128", 2),   // Tangle Angler
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "140", 2),   // Bladed Pinions
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "164", 1),   // Heavy Arbalest
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "207", 1),   // Strider Harness
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "77", 1),    // Relic Putrescence
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "115", 2),   // Carrion Call
            new DeckEntry(CardSet.MAGIC_2011, "178", 2),           // Giant Growth
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "127", 2),   // Slice in Twain
            new DeckEntry(CardSet.MAGIC_2011, "81", 2),            // Assassinate
            new DeckEntry(CardSet.MAGIC_2011, "114", 1)            // Rise from the Grave
    )),

    RELIC_BREAKER("relic-breaker", "Relic Breaker (Scars of Mirrodin Intro Pack)", List.of(
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "246", 12),  // Forest
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "242", 12),  // Mountain
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "108", 1),   // Acid Web Spider
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "83", 2),    // Barrage Ogre
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "146", 2),   // Copper Myr
            new DeckEntry(CardSet.MAGIC_2011, "167", 1),            // Cudgel Troll
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "89", 2),    // Flameborn Hellion
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "93", 1),    // Hoard-Smelter Dragon
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "168", 2),   // Iron Myr
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "125", 2),   // Molder Beast
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "101", 2),   // Oxidda Scrapmelter
            new DeckEntry(CardSet.MAGIC_2011, "152", 1),           // Prodigal Pyromancer
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "210", 2),   // Sylvok Replica
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "107", 2),   // Vulshok Heartstoker
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "221", 3),   // Vulshok Replica
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "139", 1),   // Barbed Battlegear
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "165", 1),   // Horizon Spellbomb
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "191", 1),   // Panic Spellbomb
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "110", 1),   // Asceticism
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "132", 1),   // Viridian Revel
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "103", 2),   // Shatter
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "131", 2),   // Untamed Might
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "81", 2),    // Arc Trail
            new DeckEntry(CardSet.MAGIC_2011, "138", 1),             // Fireball
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "106", 1)    // Turn to Slag
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
    )),

    BATTLE_CRIES("battle-cries", "Battle Cries (Mirrodin Besieged Intro Pack)", List.of(
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "146", 16),  // Plains
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "152", 8),   // Mountain
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "1", 2),     // Accorder Paladin
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "2", 2),     // Ardent Recruit
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "157", 1),   // Gold Myr
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "168", 2),   // Iron Myr
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "70", 3),    // Kuldotha Ringleader
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "10", 1),    // Leonin Relic-Warder
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "11", 1),    // Leonin Skyhunter
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "174", 2),   // Memnite
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "16", 1),    // Myrsmith
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "119", 2),   // Peace Strider
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "127", 1),   // Razorfield Rhino
            new DeckEntry(CardSet.MAGIC_2011, "29", 1),            // Siege Mastodon
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "131", 2),   // Signal Pest
            new DeckEntry(CardSet.MAGIC_2011, "31", 1),            // Silvercoat Lion
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "18", 1),    // Victory's Herald
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "189", 2),   // Origin Spellbomb
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "143", 1),   // Viridian Claw
            new DeckEntry(CardSet.MAGIC_2011, "221", 1),           // Whispersilk Cloak
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "2", 2),     // Arrest
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "91", 2),    // Galvanic Blast
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "13", 3),    // Master's Call
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "19", 1),    // White Sun's Zenith
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "60", 1)     // Concussive Bolt
    )),

    MIRROMANCY("mirromancy", "Mirromancy (Mirrodin Besieged Intro Pack)", List.of(
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "148", 11),  // Island
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "152", 13),  // Mountain
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "58", 2),    // Blisterstick Shaman
            // new DeckEntry(CardSet.MAGIC_2011, "137", 1),        // Fire Servant (not yet implemented)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "62", 1),    // Galvanoth
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "168", 2),   // Iron Myr
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "68", 2),    // Koth's Courier
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "112", 2),   // Lumengrid Gargoyle
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "28", 2),    // Neurok Commando
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "72", 2),    // Ogre Resister
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "119", 2),   // Peace Strider
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "202", 1),   // Silver Myr
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "59", 1),    // Burn the Impure
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "61", 1),    // Crush
            // new DeckEntry(CardSet.MAGIC_2011, "149", 1),        // Lightning Bolt (not yet implemented)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "30", 2),    // Quicksilver Geyser
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "73", 1),    // Rally the Forces
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "35", 1),    // Turn the Tide
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "81", 2),    // Arc Trail
            // new DeckEntry(CardSet.MAGIC_2011, "47", 2),         // Call to Mind (not yet implemented)
            // new DeckEntry(CardSet.MAGIC_2011, "54", 2),         // Foresee (not yet implemented)
            new DeckEntry(CardSet.MAGIC_2011, "147", 2),           // Lava Axe
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "97", 1),    // Melt Terrain
            // new DeckEntry(CardSet.MAGIC_2011, "70", 1),         // Preordain (not yet implemented)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "74", 1)     // Red Sun's Zenith
            // new DeckEntry(CardSet.MAGIC_2011, "73", 1)          // Sleep (not yet implemented)
    )),

    PATH_OF_BLIGHT("path-of-blight", "Path of Blight (Mirrodin Besieged Intro Pack)", List.of(
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "154", 14),  // Forest
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "146", 11),  // Plains
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "112", 2),   // Blight Mamba
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "77", 1),    // Blightwidow
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "103", 1),   // Core Prowler
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "147", 1),   // Corpse Cur
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "120", 2),   // Phyrexian Digester
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "85", 1),    // Phyrexian Hydra
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "121", 2),   // Phyrexian Juggernaut
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "125", 2),   // Plague Myr
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "16", 2),    // Priests of Norn
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "90", 2),    // Rot Wolf
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "128", 2),   // Tangle Angler
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "17", 2),    // Tine Shrike
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "94", 1),    // Viridian Corrupter
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "105", 1),   // Decimator Web
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "214", 2),   // Trigon of Infestation
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "3", 2),     // Banishment Decree
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "4", 2),     // Choking Fumes
            // new DeckEntry(CardSet.MAGIC_2011, "22", 1),         // Mighty Leap (not yet implemented)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "86", 1),    // Pistus Strike
            // new DeckEntry(CardSet.MAGIC_2011, "26", 1),         // Safe Passage (not yet implemented)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "93", 2)     // Unnatural Predation
            // new DeckEntry(CardSet.MAGIC_2011, "182", 2)         // Hunters' Feast (not yet implemented)
    )),

    DOOM_INEVITABLE("doom-inevitable", "Doom Inevitable (Mirrodin Besieged Intro Pack)", List.of(
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "148", 13),  // Island
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "150", 12),  // Swamp
            new DeckEntry(CardSet.MAGIC_2011, "44", 1),            // Armored Cancrix
            new DeckEntry(CardSet.MAGIC_2011, "82", 1),            // Barony Vampire
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "40", 1),    // Caustic Hound
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "63", 2),    // Fume Spitter
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "116", 2),   // Myr Sire
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "29", 3),    // Oculus
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "51", 2),    // Phyrexian Rager
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "123", 2),   // Pierce Strider
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "126", 1),   // Psychosis Crawler
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "78", 1),    // Skinrender
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "36", 1),    // Vedalken Anatomist
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "100", 1),   // Bonehoard
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "144", 2),   // Contagion Clasp
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "107", 1),   // Flayer Husk
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "133", 2),   // Skinwing
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "213", 2),   // Trigon of Corruption
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "137", 1),   // Strandwalker
            new DeckEntry(CardSet.MAGIC_2011, "67", 1),            // Mind Control
            new DeckEntry(CardSet.MAGIC_2011, "95", 1),            // Doom Blade
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "33", 1),    // Steel Sabotage
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "45", 1),    // Steady Progress
            new DeckEntry(CardSet.MAGIC_2011, "94", 1),            // Disentomb
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "45", 1),    // Horrifying Revelation
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "56", 1),    // Spread the Sickness
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "38", 2)     // Vivisection
    )),

    INFECT_AND_DEFILE("infect-and-defile", "Infect and Defile (Mirrodin Besieged Event Deck)", List.of(
            // new DeckEntry(CardSet.MAGIC_2011, "224", 2),              // Drowned Catacomb (not yet implemented)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "148", 10),         // Island
            // new DeckEntry(CardSet.ZENDIKAR, "215", 4),                // Jwar Isle Refuge (set not available)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "150", 7),          // Swamp
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "147", 4),          // Corpse Cur
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "66", 1),           // Hand of the Praetors
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "185", 4),          // Necropede
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "52", 2),           // Phyrexian Vatmother
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "125", 4),          // Plague Myr
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "144", 2),          // Contagion Clasp
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "22", 4),           // Corrupted Conscience
            // new DeckEntry(CardSet.RISE_OF_THE_ELDRAZI, "59", 2),      // Deprive (set not available)
            new DeckEntry(CardSet.MAGIC_2011, "95", 1)                     // Doom Blade
            // new DeckEntry(CardSet.MAGIC_2011, "62", 2),               // Mana Leak (not yet implemented)
            // new DeckEntry(CardSet.WORLDWAKE, "68", 2),                // Smother (set not available)
            // new DeckEntry(CardSet.RISE_OF_THE_ELDRAZI, "101", 2),     // Consuming Vapors (set not available)
            // new DeckEntry(CardSet.MAGIC_2011, "54", 4),               // Foresee (not yet implemented)
            // new DeckEntry(CardSet.MAGIC_2011, "70", 3)                // Preordain (not yet implemented)
    ), List.of(
            new DeckEntry(CardSet.MAGIC_2011, "91", 3),                   // Deathmark
            new DeckEntry(CardSet.MAGIC_2011, "95", 1),                   // Doom Blade
            new DeckEntry(CardSet.MAGIC_2011, "53", 3),                   // Flashfreeze
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "43", 2)            // Go for the Throat
            // new DeckEntry(CardSet.MAGIC_2011, "68", 4),               // Negate (not yet implemented)
            // new DeckEntry(CardSet.WORLDWAKE, "68", 2)                 // Smother (set not available)
    )),

    INTO_THE_BREACH("into-the-breach", "Into the Breach (Mirrodin Besieged Event Deck)", List.of(
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "144", 1),    // Contested War Zone
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "152", 21),   // Mountain
            // new DeckEntry(CardSet.ZENDIKAR, "125", 2),           // Goblin Bushwhacker (not yet implemented)
            // new DeckEntry(CardSet.ZENDIKAR, "126", 2),           // Goblin Guide (not yet implemented)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "64", 4),    // Goblin Wardriver
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "168", 1),   // Iron Myr
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "174", 4),   // Memnite
            new DeckEntry(CardSet.TENTH_EDITION, "336", 4),        // Ornithopter
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "131", 4),   // Signal Pest
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "104", 1),   // Spikeshot Elder
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "149", 2),   // Darksteel Axe
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "191", 2),   // Panic Spellbomb
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "91", 2),    // Galvanic Blast
            // new DeckEntry(CardSet.MAGIC_2011, "149", 4),         // Lightning Bolt (not yet implemented)
            // new DeckEntry(CardSet.RISE_OF_THE_ELDRAZI, "140", 2), // Devastating Summons (not yet implemented)
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "96", 4)     // Kuldotha Rebirth
    ), List.of(
            new DeckEntry(CardSet.MAGIC_2011, "121", 2),           // Act of Treason
            // new DeckEntry(CardSet.ZENDIKAR, "127", 4),           // Goblin Ruinblaster (not yet implemented)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "67", 2)     // Into the Core
            // new DeckEntry(CardSet.MAGIC_2011, "148", 1),         // Leyline of Punishment (not yet implemented)
            // new DeckEntry(CardSet.WORLDWAKE, "90", 4),           // Searing Blaze (not yet implemented)
            // new DeckEntry(CardSet.ZENDIKAR, "153", 2)            // Unstable Footing (not yet implemented)
    )),

    ARTFUL_DESTRUCTION("artful-destruction", "Artful Destruction (New Phyrexia Intro Pack)", List.of(
            new DeckEntry(CardSet.NEW_PHYREXIA, "174", 12),         // Forest
            new DeckEntry(CardSet.NEW_PHYREXIA, "166", 12),         // Plains
            new DeckEntry(CardSet.NEW_PHYREXIA, "4", 1),            // Blade Splicer
            new DeckEntry(CardSet.NEW_PHYREXIA, "105", 1),          // Brutalizer Exarch
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "146", 3),     // Copper Myr
            // new DeckEntry(CardSet.MAGIC_2011, "177", 1),          // Garruk's Packleader (not yet implemented)
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "157", 3),     // Gold Myr
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "159", 1),     // Golem Artisan
            new DeckEntry(CardSet.NEW_PHYREXIA, "16", 3),           // Master Splicer
            new DeckEntry(CardSet.NEW_PHYREXIA, "114", 1),          // Maul Splicer
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "190", 2),     // Palladium Myr
            new DeckEntry(CardSet.NEW_PHYREXIA, "150", 1),          // Phyrexian Hulk
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "194", 1),     // Precursor Golem
            new DeckEntry(CardSet.NEW_PHYREXIA, "22", 2),           // Sensor Splicer
            // new DeckEntry(CardSet.MAGIC_2011, "215", 1),          // Stone Golem (not yet implemented)
            new DeckEntry(CardSet.NEW_PHYREXIA, "25", 2),           // Suture Priest
            new DeckEntry(CardSet.NEW_PHYREXIA, "126", 2),          // Vital Splicer
            new DeckEntry(CardSet.NEW_PHYREXIA, "133", 1),          // Conversion Chamber
            new DeckEntry(CardSet.NEW_PHYREXIA, "11", 2),           // Forced Worship
            new DeckEntry(CardSet.NEW_PHYREXIA, "125", 1),          // Viridian Harvest
            new DeckEntry(CardSet.MAGIC_2011, "178", 1),            // Giant Growth
            new DeckEntry(CardSet.NEW_PHYREXIA, "110", 1),          // Glissa's Scorn
            // new DeckEntry(CardSet.MAGIC_2011, "22", 1),           // Mighty Leap (not yet implemented)
            new DeckEntry(CardSet.NEW_PHYREXIA, "26", 2)              // War Report
            // new DeckEntry(CardSet.MAGIC_2011, "168", 2)           // Cultivate (not yet implemented)
    )),

    DEVOURING_SKIES("devouring-skies", "Devouring Skies (New Phyrexia Intro Pack)", List.of(
            new DeckEntry(CardSet.NEW_PHYREXIA, "168", 13),         // Island
            new DeckEntry(CardSet.NEW_PHYREXIA, "170", 11),         // Swamp
            new DeckEntry(CardSet.MAGIC_2011, "45", 2),             // Augury Owl
            new DeckEntry(CardSet.NEW_PHYREXIA, "52", 2),           // Blind Zealot
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "101", 2),     // Brass Squire
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "30", 1),      // Darkslick Drake
            new DeckEntry(CardSet.NEW_PHYREXIA, "55", 2),           // Dementia Bat
            new DeckEntry(CardSet.NEW_PHYREXIA, "138", 3),          // Hovermyr
            new DeckEntry(CardSet.NEW_PHYREXIA, "36", 2),           // Impaler Shrike
            new DeckEntry(CardSet.NEW_PHYREXIA, "142", 1),          // Kiln Walker
            new DeckEntry(CardSet.NEW_PHYREXIA, "66", 2),           // Mortis Dogs
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "71", 1),      // Necrogen Scudder
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "37", 1),      // Neurok Invisimancer
            new DeckEntry(CardSet.NEW_PHYREXIA, "41", 1),           // Phyrexian Ingester
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "202", 1),     // Silver Myr
            new DeckEntry(CardSet.NEW_PHYREXIA, "46", 2),           // Spire Monitor
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "137", 1),     // Argentum Armor
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "102", 2),     // Copper Carapace
            new DeckEntry(CardSet.NEW_PHYREXIA, "147", 1),          // Necropouncer
            new DeckEntry(CardSet.NEW_PHYREXIA, "157", 2),          // Sickleslicer
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "143", 2),     // Viridian Claw
            // new DeckEntry(CardSet.MAGIC_2012, "221", 2),          // Warlord's Axe (not yet implemented)
            new DeckEntry(CardSet.MAGIC_2011, "95", 1),             // Doom Blade
            new DeckEntry(CardSet.NEW_PHYREXIA, "48", 2)            // Vapor Snag
    )),

    LIFE_FOR_DEATH("life-for-death", "Life for Death (New Phyrexia Intro Pack)", List.of(
            new DeckEntry(CardSet.NEW_PHYREXIA, "172", 11),           // Mountain
            new DeckEntry(CardSet.NEW_PHYREXIA, "166", 13),           // Plains
            new DeckEntry(CardSet.NEW_PHYREXIA, "131", 2),            // Blinding Souleater
            new DeckEntry(CardSet.NEW_PHYREXIA, "5", 1),              // Cathedral Membrane
            new DeckEntry(CardSet.NEW_PHYREXIA, "139", 2),            // Immolating Souleater
            new DeckEntry(CardSet.NEW_PHYREXIA, "12", 1),             // Inquisitor Exarch
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "13", 2),        // Kemba's Skyguard
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "112", 1),       // Lumengrid Gargoyle
            new DeckEntry(CardSet.NEW_PHYREXIA, "88", 1),             // Moltensteel Dragon
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "72", 1),        // Ogre Resister
            new DeckEntry(CardSet.NEW_PHYREXIA, "19", 3),             // Porcelain Legionnaire
            new DeckEntry(CardSet.NEW_PHYREXIA, "23", 1),             // Shattered Angel
            new DeckEntry(CardSet.NEW_PHYREXIA, "96", 3),             // Slash Panther
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "161", 2),       // Golem's Heart
            new DeckEntry(CardSet.NEW_PHYREXIA, "151", 1),            // Pristine Talisman
            new DeckEntry(CardSet.NEW_PHYREXIA, "91", 2),             // Rage Extractor
            new DeckEntry(CardSet.MAGIC_2011, "23", 2),               // Pacifism
            new DeckEntry(CardSet.NEW_PHYREXIA, "78", 2),             // Act of Aggression
            new DeckEntry(CardSet.NEW_PHYREXIA, "2", 1),              // Apostle's Blessing
            new DeckEntry(CardSet.NEW_PHYREXIA, "86", 1),             // Gut Shot
            // new DeckEntry(CardSet.MAGIC_2011, "145", 1),           // Incite (not yet implemented)
            // new DeckEntry(CardSet.MAGIC_2011, "149", 1),           // Lightning Bolt (not yet implemented)
            new DeckEntry(CardSet.NEW_PHYREXIA, "15", 1),             // Marrow Shards
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "27", 2),        // Whitesun's Passage
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "15", 1)         // Phyrexian Rebirth
            // new DeckEntry(CardSet.MAGIC_2011, "32", 1)             // Solemn Offering (not yet implemented)
    )),

    FEAST_OF_FLESH("feast-of-flesh", "Feast of Flesh (New Phyrexia Intro Pack)", List.of(
            new DeckEntry(CardSet.NEW_PHYREXIA, "172", 11),            // Mountain
            new DeckEntry(CardSet.NEW_PHYREXIA, "170", 13),            // Swamp
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "56", 2),         // Blistergrub
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "58", 2),         // Blisterstick Shaman
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "40", 2),         // Caustic Hound
            new DeckEntry(CardSet.NEW_PHYREXIA, "54", 1),              // Chancellor of the Dross
            new DeckEntry(CardSet.NEW_PHYREXIA, "59", 2),              // Entomber Exarch
            new DeckEntry(CardSet.NEW_PHYREXIA, "83", 2),              // Flameborn Viron
            new DeckEntry(CardSet.NEW_PHYREXIA, "84", 2),              // Furnace Scamp
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "51", 2),         // Phyrexian Rager
            new DeckEntry(CardSet.MAGIC_2011, "152", 1),               // Prodigal Pyromancer
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "102", 1),        // Scoria Elemental
            new DeckEntry(CardSet.NEW_PHYREXIA, "97", 2),              // Tormentor Exarch
            new DeckEntry(CardSet.NEW_PHYREXIA, "153", 2),             // Shrine of Burning Rage
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "212", 1),        // Tower of Calamities
            new DeckEntry(CardSet.NEW_PHYREXIA, "58", 1),              // Enslave
            new DeckEntry(CardSet.NEW_PHYREXIA, "67", 2),              // Parasitic Implant
            new DeckEntry(CardSet.NEW_PHYREXIA, "79", 2),              // Artillerize
            // new DeckEntry(CardSet.MAGIC_2011, "140", 1),            // Fling (not yet implemented)
            new DeckEntry(CardSet.NEW_PHYREXIA, "61", 2),              // Geth's Verdict
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "43", 1),         // Go for the Throat
            new DeckEntry(CardSet.NEW_PHYREXIA, "56", 1),              // Despise
            // new DeckEntry(CardSet.MAGIC_2011, "94", 1),             // Disentomb (not yet implemented)
            new DeckEntry(CardSet.NEW_PHYREXIA, "64", 1),              // Ichor Explosion
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "47", 1),         // Morbid Plunder
            new DeckEntry(CardSet.NEW_PHYREXIA, "102", 1)              // Whipflare
    )),

    RAVAGING_SWARM("ravaging-swarm", "Ravaging Swarm (New Phyrexia Intro Pack)", List.of(
            new DeckEntry(CardSet.NEW_PHYREXIA, "174", 12),              // Forest
            new DeckEntry(CardSet.NEW_PHYREXIA, "168", 12),              // Island
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "112", 1),          // Blight Mamba
            new DeckEntry(CardSet.NEW_PHYREXIA, "29", 3),                // Blighted Agent
            new DeckEntry(CardSet.NEW_PHYREXIA, "30", 2),                // Chained Throatseeker
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "103", 1),          // Core Prowler
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "117", 2),          // Cystbearer
            new DeckEntry(CardSet.NEW_PHYREXIA, "111", 3),               // Glistener Elf
            new DeckEntry(CardSet.NEW_PHYREXIA, "117", 2),               // Mycosynth Fiend
            new DeckEntry(CardSet.NEW_PHYREXIA, "119", 1),               // Phyrexian Swarmlord
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "87", 1),           // Plaguemaw Beast
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "90", 1),           // Rot Wolf
            new DeckEntry(CardSet.NEW_PHYREXIA, "121", 1),               // Spinebiter
            new DeckEntry(CardSet.NEW_PHYREXIA, "49", 2),                // Viral Drake
            new DeckEntry(CardSet.NEW_PHYREXIA, "124", 2),               // Viridian Betrayers
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "222", 1),          // Wall of Tanglecord
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "144", 1),          // Contagion Clasp
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "214", 2),          // Trigon of Infestation
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "22", 1),           // Corrupted Conscience
            new DeckEntry(CardSet.NEW_PHYREXIA, "34", 1),                // Defensive Stance
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "35", 1),           // Inexorable Tide
            new DeckEntry(CardSet.NEW_PHYREXIA, "32", 1),                // Corrupted Resolve
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "25", 1),           // Fuel for the Cause
            new DeckEntry(CardSet.NEW_PHYREXIA, "113", 3),               // Leeching Bite
            new DeckEntry(CardSet.TENTH_EDITION, "282", 1),              // Naturalize
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "45", 1)            // Steady Progress
    )),

    ROT_FROM_WITHIN("rot-from-within", "Rot from Within (New Phyrexia Event Deck)", List.of(
            new DeckEntry(CardSet.NEW_PHYREXIA, "174", 22),            // Forest
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "145", 1),        // Inkmoth Nexus
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "112", 1),        // Blight Mamba
            new DeckEntry(CardSet.NEW_PHYREXIA, "111", 4),             // Glistener Elf
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "166", 2),        // Ichorclaw Myr
            // new DeckEntry(CardSet.RISE_OF_THE_ELDRAZI, "203", 4),   // Overgrown Battlement (set not available)
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "126", 2),        // Putrefax
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "90", 3),         // Rot Wolf
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "94", 3),         // Viridian Corrupter
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "144", 1),        // Contagion Clasp
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "115", 4),        // Carrion Call
            // new DeckEntry(CardSet.WORLDWAKE, "104", 4),              // Groundswell (set not available)
            new DeckEntry(CardSet.NEW_PHYREXIA, "116", 4),             // Mutagenic Growth
            // new DeckEntry(CardSet.ZENDIKAR, "176", 4),               // Primal Bellow (set not available)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "81", 1)          // Green Sun's Zenith
    ), List.of(
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "144", 3),        // Contagion Clasp
            new DeckEntry(CardSet.NEW_PHYREXIA, "115", 1),             // Melira, Sylvok Outcast
            // new DeckEntry(CardSet.MAGIC_2011, "188", 2),             // Obstinate Baloth (not yet implemented)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "86", 1),         // Pistus Strike
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "214", 3),        // Trigon of Infestation
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "93", 2),         // Unnatural Predation
            // new DeckEntry(CardSet.ZENDIKAR, "193", 2),               // Vines of Vastwood (set not available)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "94", 1)          // Viridian Corrupter
    )),

    WAR_OF_ATTRITION("war-of-attrition", "War of Attrition (New Phyrexia Event Deck)", List.of(
            // new DeckEntry(CardSet.WORLDWAKE, "161", 2),                 // Dread Statuary (set not available)
            new DeckEntry(CardSet.NEW_PHYREXIA, "166", 21),              // Plains
            new DeckEntry(CardSet.MAGIC_2011, "13", 4),                  // Elite Vanguard
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "12", 1),           // Kemba, Kha Regent
            // new DeckEntry(CardSet.ZENDIKAR, "10", 2),                   // Kor Duelist (set not available)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "10", 4),           // Leonin Relic-Warder
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "11", 4),           // Leonin Skyhunter
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "14", 1),           // Mirran Crusader
            new DeckEntry(CardSet.NEW_PHYREXIA, "19", 4),                // Porcelain Legionnaire
            new DeckEntry(CardSet.NEW_PHYREXIA, "20", 1),                // Puresteel Paladin
            // new DeckEntry(CardSet.WORLDWAKE, "20", 2),                  // Stoneforge Mystic (set not available)
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "100", 1),          // Bonehoard
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "149", 1),          // Darksteel Axe
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "107", 4),          // Flayer Husk
            new DeckEntry(CardSet.NEW_PHYREXIA, "157", 1),               // Sickleslicer
            new DeckEntry(CardSet.MIRRODIN_BESIEGED, "133", 1),          // Skinwing
            // new DeckEntry(CardSet.MAGIC_2011, "216", 1),                // Sword of Vengeance (not yet implemented)
            // new DeckEntry(CardSet.ZENDIKAR, "14", 4),                   // Journey to Nowhere (set not available)
            new DeckEntry(CardSet.NEW_PHYREXIA, "2", 1)                  // Apostle's Blessing
    ), List.of(
            new DeckEntry(CardSet.NEW_PHYREXIA, "2", 1),                 // Apostle's Blessing
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "2", 2),            // Arrest
            new DeckEntry(CardSet.MAGIC_2011, "9", 3),                   // Celestial Purge
            // new DeckEntry(CardSet.ZENDIKAR, "10", 1),                   // Kor Duelist (set not available)
            // new DeckEntry(CardSet.WORLDWAKE, "11", 4),                  // Kor Firewalker (set not available)
            new DeckEntry(CardSet.SCARS_OF_MIRRODIN, "18", 4)            // Revoke Existence
    ));

    public record DeckEntry(CardSet cardSet, String collectorNumber, int count) {}

    private final String id;
    private final String name;
    private final List<DeckEntry> entries;
    private final List<DeckEntry> sideboard;

    PrebuiltDeck(String id, String name, List<DeckEntry> entries) {
        this(id, name, entries, List.of());
    }

    PrebuiltDeck(String id, String name, List<DeckEntry> entries, List<DeckEntry> sideboard) {
        this.id = id;
        this.name = name;
        this.entries = entries;
        this.sideboard = sideboard;
    }

    public List<Card> buildDeck() {
        return buildCards(entries);
    }

    public List<Card> buildSideboard() {
        return buildCards(sideboard);
    }

    private List<Card> buildCards(List<DeckEntry> deckEntries) {
        List<Card> cards = new ArrayList<>();
        for (DeckEntry entry : deckEntries) {
            CardPrinting printing = entry.cardSet().findByCollectorNumber(entry.collectorNumber());
            for (int i = 0; i < entry.count(); i++) {
                cards.add(printing.createCard());
            }
        }
        return cards;
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
