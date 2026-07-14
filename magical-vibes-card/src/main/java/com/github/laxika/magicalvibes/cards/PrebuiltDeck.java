package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.model.Card;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public enum PrebuiltDeck {

    _10E_WHITE_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_10E, "364", 17),  // Plains
            new DeckEntry(CardSet.SET_10E, "16", 1),    // Ghost Warden
            new DeckEntry(CardSet.SET_10E, "62", 2),    // Youthful Knight
            new DeckEntry(CardSet.SET_10E, "11", 2),    // Benalish Knight
            new DeckEntry(CardSet.SET_10E, "55", 1),    // Venerable Monk
            new DeckEntry(CardSet.SET_10E, "59", 2),    // Wild Griffin
            new DeckEntry(CardSet.SET_10E, "12", 1),    // Cho-Manno, Revolutionary
            new DeckEntry(CardSet.SET_10E, "41", 2),    // Skyhunter Patrol
            new DeckEntry(CardSet.SET_10E, "2", 2),     // Angel of Mercy
            new DeckEntry(CardSet.SET_10E, "26", 2),    // Loxodon Mystic
            new DeckEntry(CardSet.SET_10E, "1", 1),     // Ancestor's Chosen
            new DeckEntry(CardSet.SET_10E, "13", 1),    // Condemn
            new DeckEntry(CardSet.SET_10E, "31", 2),    // Pacifism
            new DeckEntry(CardSet.SET_10E, "33", 1),    // Pariah
            new DeckEntry(CardSet.SET_10E, "40", 1),    // Serra's Embrace
            new DeckEntry(CardSet.SET_10E, "311", 1),   // Angel's Feather
            new DeckEntry(CardSet.SET_10E, "326", 1)    // Icy Manipulator
    )),

    _10E_BLUE_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_10E, "368", 17),  // Island
            new DeckEntry(CardSet.SET_10E, "104", 2),   // Sage Owl
            new DeckEntry(CardSet.SET_10E, "74", 2),    // Cloud Elemental
            new DeckEntry(CardSet.SET_10E, "96", 1),    // Phantom Warrior
            new DeckEntry(CardSet.SET_10E, "68", 1),    // Aven Fisher
            new DeckEntry(CardSet.SET_10E, "115", 1),   // Thieving Magpie
            new DeckEntry(CardSet.SET_10E, "64", 1),    // Air Elemental
            new DeckEntry(CardSet.SET_10E, "66", 1),    // Arcanis the Omnipotent
            new DeckEntry(CardSet.SET_10E, "80", 1),    // Denizen of the Deep
            new DeckEntry(CardSet.SET_10E, "122", 2),   // Unsummon
            new DeckEntry(CardSet.SET_10E, "100", 2),   // Remove Soul
            new DeckEntry(CardSet.SET_10E, "114", 1),   // Telling Time
            new DeckEntry(CardSet.SET_10E, "70", 1),    // Boomerang
            new DeckEntry(CardSet.SET_10E, "76", 2),    // Counsel of the Soratami
            new DeckEntry(CardSet.SET_10E, "71", 2),    // Cancel
            new DeckEntry(CardSet.SET_10E, "116", 1),   // Tidings
            new DeckEntry(CardSet.SET_10E, "329", 1),   // Kraken's Eye
            new DeckEntry(CardSet.SET_10E, "341", 1)    // Rod of Ruin
    )),

    _10E_RED_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_10E, "376", 17),  // Mountain
            new DeckEntry(CardSet.SET_10E, "224", 1),   // Raging Goblin
            new DeckEntry(CardSet.SET_10E, "246", 1),   // Viashino Sandscout
            new DeckEntry(CardSet.SET_10E, "192", 2),   // Bloodrock Cyclops
            new DeckEntry(CardSet.SET_10E, "193", 2),   // Bogardan Firefiend
            new DeckEntry(CardSet.SET_10E, "221", 1),   // Prodigal Pyromancer
            new DeckEntry(CardSet.SET_10E, "217", 2),   // Lightning Elemental
            new DeckEntry(CardSet.SET_10E, "205", 1),   // Furnace Whelp
            new DeckEntry(CardSet.SET_10E, "243", 2),   // Thundering Giant
            new DeckEntry(CardSet.SET_10E, "214", 1),   // Kamahl, Pit Fighter
            new DeckEntry(CardSet.SET_10E, "232", 1),   // Shock
            new DeckEntry(CardSet.SET_10E, "213", 2),   // Incinerate
            new DeckEntry(CardSet.SET_10E, "189", 1),   // Beacon of Destruction
            new DeckEntry(CardSet.SET_10E, "190", 1),   // Blaze
            new DeckEntry(CardSet.SET_10E, "238", 2),   // Spitting Earth
            new DeckEntry(CardSet.SET_10E, "242", 1),   // Threaten
            new DeckEntry(CardSet.SET_10E, "322", 1),   // Dragon's Claw
            new DeckEntry(CardSet.SET_10E, "337", 1)    // Phyrexian Vault
    )),

    SOM_BLUE_BLACK_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_SOM, "234", 13),  // Island
            new DeckEntry(CardSet.SET_SOM, "238", 13),  // Swamp
            new DeckEntry(CardSet.SET_SOM, "57", 1),    // Carnifex Demon
            new DeckEntry(CardSet.SET_SOM, "30", 1),    // Darkslick Drake
            new DeckEntry(CardSet.SET_SOM, "63", 2),    // Fume Spitter
            new DeckEntry(CardSet.SET_M11, "56", 1),            // Harbor Serpent
            new DeckEntry(CardSet.SET_SOM, "170", 2),   // Leaden Myr
            new DeckEntry(CardSet.SET_M11, "63", 2),            // Maritime Guard
            new DeckEntry(CardSet.SET_SOM, "70", 2),    // Moriok Reaver
            new DeckEntry(CardSet.SET_SOM, "202", 2),   // Silver Myr
            new DeckEntry(CardSet.SET_SOM, "78", 1),    // Skinrender
            new DeckEntry(CardSet.SET_SOM, "44", 2),    // Sky-Eel School
            new DeckEntry(CardSet.SET_SOM, "47", 2),    // Thrummingbird
            new DeckEntry(CardSet.SET_SOM, "144", 2),   // Contagion Clasp
            new DeckEntry(CardSet.SET_SOM, "145", 1),   // Contagion Engine
            new DeckEntry(CardSet.SET_SOM, "148", 1),   // Culling Dais
            new DeckEntry(CardSet.SET_SOM, "184", 2),   // Necrogen Censer
            new DeckEntry(CardSet.SET_M11, "213", 1),           // Sorcerer's Strongbox
            new DeckEntry(CardSet.SET_SOM, "211", 1),   // Throne of Geth
            new DeckEntry(CardSet.SET_SOM, "213", 1),   // Trigon of Corruption
            new DeckEntry(CardSet.SET_SOM, "217", 1),   // Trigon of Thought
            new DeckEntry(CardSet.SET_M11, "95", 1),            // Doom Blade
            new DeckEntry(CardSet.SET_SOM, "68", 2),    // Instill Infection
            new DeckEntry(CardSet.SET_SOM, "45", 2),    // Steady Progress
            new DeckEntry(CardSet.SET_SOM, "50", 1)     // Twisted Image
    )),

    SOM_BLUE_RED_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_SOM, "234", 12),  // Island
            new DeckEntry(CardSet.SET_SOM, "242", 12),  // Mountain
            new DeckEntry(CardSet.SET_SOM, "28", 1),    // Argent Sphinx
            new DeckEntry(CardSet.SET_SOM, "84", 2),    // Blade-Tribe Berserkers
            new DeckEntry(CardSet.SET_SOM, "142", 3),   // Chrome Steed
            new DeckEntry(CardSet.SET_SOM, "87", 2),    // Embersmith
            new DeckEntry(CardSet.SET_SOM, "154", 1),   // Etched Champion
            new DeckEntry(CardSet.SET_SOM, "168", 2),   // Iron Myr
            new DeckEntry(CardSet.SET_SOM, "36", 2),    // Lumengrid Drake
            new DeckEntry(CardSet.SET_SOM, "174", 2),   // Memnite
            new DeckEntry(CardSet.SET_SOM, "40", 2),    // Riddlesmith
            new DeckEntry(CardSet.SET_SOM, "202", 2),   // Silver Myr
            new DeckEntry(CardSet.SET_SOM, "203", 3),   // Snapsail Glider
            new DeckEntry(CardSet.SET_SOM, "48", 2),    // Trinket Mage
            new DeckEntry(CardSet.SET_SOM, "136", 1),   // Accorder's Shield
            new DeckEntry(CardSet.SET_SOM, "149", 1),   // Darksteel Axe
            new DeckEntry(CardSet.SET_SOM, "153", 1),   // Echo Circlet
            new DeckEntry(CardSet.SET_SOM, "160", 1),   // Golem Foundry
            new DeckEntry(CardSet.SET_SOM, "161", 1),   // Golem's Heart
            new DeckEntry(CardSet.SET_SOM, "199", 2),   // Rusted Relic
            new DeckEntry(CardSet.SET_SOM, "209", 1),   // Sylvok Lifestaff
            new DeckEntry(CardSet.SET_SOM, "31", 1),    // Disperse
            new DeckEntry(CardSet.SET_SOM, "91", 3)     // Galvanic Blast
    )),

    SOM_WHITE_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_SOM, "230", 24),  // Plains
            new DeckEntry(CardSet.SET_SOM, "3", 1),     // Auriok Edgewright
            new DeckEntry(CardSet.SET_SOM, "151", 2),   // Darksteel Myr
            new DeckEntry(CardSet.SET_SOM, "152", 1),   // Darksteel Sentinel
            new DeckEntry(CardSet.SET_SOM, "8", 2),     // Ghalma's Warden
            new DeckEntry(CardSet.SET_SOM, "157", 3),   // Gold Myr
            new DeckEntry(CardSet.SET_SOM, "13", 2),    // Kemba's Skyguard
            new DeckEntry(CardSet.SET_SOM, "180", 1),   // Myr Battlesphere
            new DeckEntry(CardSet.SET_SOM, "181", 2),   // Myr Galvanizer
            new DeckEntry(CardSet.SET_SOM, "16", 2),    // Myrsmith
            new DeckEntry(CardSet.SET_SOM, "190", 2),   // Palladium Myr
            new DeckEntry(CardSet.SET_SOM, "192", 2),   // Perilous Myr
            new DeckEntry(CardSet.SET_SOM, "17", 1),    // Razor Hippogriff
            new DeckEntry(CardSet.SET_SOM, "22", 1),    // Sunblast Angel
            new DeckEntry(CardSet.SET_SOM, "189", 3),   // Origin Spellbomb
            new DeckEntry(CardSet.SET_SOM, "2", 3),     // Arrest
            new DeckEntry(CardSet.SET_SOM, "5", 1),     // Dispense Justice
            new DeckEntry(CardSet.SET_M11, "19", 2),             // Inspired Charge
            new DeckEntry(CardSet.SET_SOM, "20", 2),    // Seize the Initiative
            new DeckEntry(CardSet.SET_SOM, "18", 3)     // Revoke Existence
    )),

    SOM_BLACK_GREEN_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_SOM, "246", 13),  // Forest
            new DeckEntry(CardSet.SET_SOM, "238", 13),  // Swamp
            new DeckEntry(CardSet.SET_SOM, "54", 1),    // Blackcleave Goblin
            new DeckEntry(CardSet.SET_SOM, "58", 1),    // Contagious Nim
            new DeckEntry(CardSet.SET_SOM, "147", 2),   // Corpse Cur
            new DeckEntry(CardSet.SET_SOM, "117", 2),   // Cystbearer
            new DeckEntry(CardSet.SET_SOM, "66", 1),    // Hand of the Praetors
            new DeckEntry(CardSet.SET_SOM, "67", 2),    // Ichor Rats
            new DeckEntry(CardSet.SET_SOM, "166", 3),   // Ichorclaw Myr
            new DeckEntry(CardSet.SET_SOM, "185", 2),   // Necropede
            new DeckEntry(CardSet.SET_SOM, "75", 3),    // Plague Stinger
            new DeckEntry(CardSet.SET_SOM, "126", 1),   // Putrefax
            new DeckEntry(CardSet.SET_SOM, "128", 2),   // Tangle Angler
            new DeckEntry(CardSet.SET_SOM, "140", 2),   // Bladed Pinions
            new DeckEntry(CardSet.SET_SOM, "164", 1),   // Heavy Arbalest
            new DeckEntry(CardSet.SET_SOM, "207", 1),   // Strider Harness
            new DeckEntry(CardSet.SET_SOM, "77", 1),    // Relic Putrescence
            new DeckEntry(CardSet.SET_SOM, "115", 2),   // Carrion Call
            new DeckEntry(CardSet.SET_M11, "178", 2),           // Giant Growth
            new DeckEntry(CardSet.SET_SOM, "127", 2),   // Slice in Twain
            new DeckEntry(CardSet.SET_M11, "81", 2),            // Assassinate
            new DeckEntry(CardSet.SET_M11, "114", 1)            // Rise from the Grave
    )),

    SOM_RED_GREEN_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_SOM, "246", 12),  // Forest
            new DeckEntry(CardSet.SET_SOM, "242", 12),  // Mountain
            new DeckEntry(CardSet.SET_SOM, "108", 1),   // Acid Web Spider
            new DeckEntry(CardSet.SET_SOM, "83", 2),    // Barrage Ogre
            new DeckEntry(CardSet.SET_SOM, "146", 2),   // Copper Myr
            new DeckEntry(CardSet.SET_M11, "167", 1),            // Cudgel Troll
            new DeckEntry(CardSet.SET_SOM, "89", 2),    // Flameborn Hellion
            new DeckEntry(CardSet.SET_SOM, "93", 1),    // Hoard-Smelter Dragon
            new DeckEntry(CardSet.SET_SOM, "168", 2),   // Iron Myr
            new DeckEntry(CardSet.SET_SOM, "125", 2),   // Molder Beast
            new DeckEntry(CardSet.SET_SOM, "101", 2),   // Oxidda Scrapmelter
            new DeckEntry(CardSet.SET_M11, "152", 1),           // Prodigal Pyromancer
            new DeckEntry(CardSet.SET_SOM, "210", 2),   // Sylvok Replica
            new DeckEntry(CardSet.SET_SOM, "107", 2),   // Vulshok Heartstoker
            new DeckEntry(CardSet.SET_SOM, "221", 3),   // Vulshok Replica
            new DeckEntry(CardSet.SET_SOM, "139", 1),   // Barbed Battlegear
            new DeckEntry(CardSet.SET_SOM, "165", 1),   // Horizon Spellbomb
            new DeckEntry(CardSet.SET_SOM, "191", 1),   // Panic Spellbomb
            new DeckEntry(CardSet.SET_SOM, "110", 1),   // Asceticism
            new DeckEntry(CardSet.SET_SOM, "132", 1),   // Viridian Revel
            new DeckEntry(CardSet.SET_SOM, "103", 2),   // Shatter
            new DeckEntry(CardSet.SET_SOM, "131", 2),   // Untamed Might
            new DeckEntry(CardSet.SET_SOM, "81", 2),    // Arc Trail
            new DeckEntry(CardSet.SET_M11, "138", 1),             // Fireball
            new DeckEntry(CardSet.SET_SOM, "106", 1)    // Turn to Slag
    )),

    _10E_BLACK_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_10E, "372", 17),  // Swamp
            new DeckEntry(CardSet.SET_10E, "186", 2),   // Vampire Bats
            new DeckEntry(CardSet.SET_10E, "139", 1),   // Drudge Skeletons
            new DeckEntry(CardSet.SET_10E, "183", 1),   // Thrull Surgeon
            new DeckEntry(CardSet.SET_10E, "153", 1),   // Looming Shade
            new DeckEntry(CardSet.SET_10E, "177", 2),   // Severed Legion
            new DeckEntry(CardSet.SET_10E, "138", 1),   // Dross Crocodile
            new DeckEntry(CardSet.SET_10E, "146", 1),   // Gravedigger
            new DeckEntry(CardSet.SET_10E, "161", 1),   // Mortivore
            new DeckEntry(CardSet.SET_10E, "156", 1),   // Mass of Ghouls
            new DeckEntry(CardSet.SET_10E, "127", 1),   // Ascendant Evincar
            new DeckEntry(CardSet.SET_10E, "185", 1),   // Unholy Strength
            new DeckEntry(CardSet.SET_10E, "182", 1),   // Terror
            new DeckEntry(CardSet.SET_10E, "133", 1),   // Cruel Edict
            new DeckEntry(CardSet.SET_10E, "135", 1),   // Diabolic Tutor
            new DeckEntry(CardSet.SET_10E, "141", 1),   // Essence Drain
            new DeckEntry(CardSet.SET_10E, "131", 1),   // Consume Spirit
            new DeckEntry(CardSet.SET_10E, "128", 1),   // Assassinate
            new DeckEntry(CardSet.SET_10E, "159", 2),   // Mind Rot
            new DeckEntry(CardSet.SET_10E, "312", 1),   // Bottle Gnomes
            new DeckEntry(CardSet.SET_10E, "320", 1)    // Demon's Horn
    )),

    MBS_WHITE_RED_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_MBS, "146", 16),  // Plains
            new DeckEntry(CardSet.SET_MBS, "152", 8),   // Mountain
            new DeckEntry(CardSet.SET_MBS, "1", 2),     // Accorder Paladin
            new DeckEntry(CardSet.SET_MBS, "2", 2),     // Ardent Recruit
            new DeckEntry(CardSet.SET_SOM, "157", 1),   // Gold Myr
            new DeckEntry(CardSet.SET_SOM, "168", 2),   // Iron Myr
            new DeckEntry(CardSet.SET_MBS, "70", 3),    // Kuldotha Ringleader
            new DeckEntry(CardSet.SET_MBS, "10", 1),    // Leonin Relic-Warder
            new DeckEntry(CardSet.SET_MBS, "11", 1),    // Leonin Skyhunter
            new DeckEntry(CardSet.SET_SOM, "174", 2),   // Memnite
            new DeckEntry(CardSet.SET_SOM, "16", 1),    // Myrsmith
            new DeckEntry(CardSet.SET_MBS, "119", 2),   // Peace Strider
            new DeckEntry(CardSet.SET_MBS, "127", 1),   // Razorfield Rhino
            new DeckEntry(CardSet.SET_M11, "29", 1),            // Siege Mastodon
            new DeckEntry(CardSet.SET_MBS, "131", 2),   // Signal Pest
            new DeckEntry(CardSet.SET_M11, "31", 1),            // Silvercoat Lion
            new DeckEntry(CardSet.SET_MBS, "18", 1),    // Victory's Herald
            new DeckEntry(CardSet.SET_SOM, "189", 2),   // Origin Spellbomb
            new DeckEntry(CardSet.SET_MBS, "143", 1),   // Viridian Claw
            new DeckEntry(CardSet.SET_M11, "221", 1),           // Whispersilk Cloak
            new DeckEntry(CardSet.SET_SOM, "2", 2),     // Arrest
            new DeckEntry(CardSet.SET_SOM, "91", 2),    // Galvanic Blast
            new DeckEntry(CardSet.SET_MBS, "13", 3),    // Master's Call
            new DeckEntry(CardSet.SET_MBS, "19", 1),    // White Sun's Zenith
            new DeckEntry(CardSet.SET_MBS, "60", 1)     // Concussive Bolt
    )),

    MBS_BLUE_RED_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_MBS, "148", 11),  // Island
            new DeckEntry(CardSet.SET_MBS, "152", 13),  // Mountain
            new DeckEntry(CardSet.SET_MBS, "58", 2),    // Blisterstick Shaman
            // new DeckEntry(CardSet.SET_M11, "137", 1),        // Fire Servant (not yet implemented)
            new DeckEntry(CardSet.SET_MBS, "62", 1),    // Galvanoth
            new DeckEntry(CardSet.SET_SOM, "168", 2),   // Iron Myr
            new DeckEntry(CardSet.SET_MBS, "68", 2),    // Koth's Courier
            new DeckEntry(CardSet.SET_MBS, "112", 2),   // Lumengrid Gargoyle
            new DeckEntry(CardSet.SET_MBS, "28", 2),    // Neurok Commando
            new DeckEntry(CardSet.SET_MBS, "72", 2),    // Ogre Resister
            new DeckEntry(CardSet.SET_MBS, "119", 2),   // Peace Strider
            new DeckEntry(CardSet.SET_SOM, "202", 1),   // Silver Myr
            new DeckEntry(CardSet.SET_MBS, "59", 1),    // Burn the Impure
            new DeckEntry(CardSet.SET_MBS, "61", 1),    // Crush
            // new DeckEntry(CardSet.SET_M11, "149", 1),        // Lightning Bolt (not yet implemented)
            new DeckEntry(CardSet.SET_MBS, "30", 2),    // Quicksilver Geyser
            new DeckEntry(CardSet.SET_MBS, "73", 1),    // Rally the Forces
            new DeckEntry(CardSet.SET_MBS, "35", 1),    // Turn the Tide
            new DeckEntry(CardSet.SET_SOM, "81", 2),    // Arc Trail
            // new DeckEntry(CardSet.SET_M11, "47", 2),         // Call to Mind (not yet implemented)
            // new DeckEntry(CardSet.SET_M11, "54", 2),         // Foresee (not yet implemented)
            new DeckEntry(CardSet.SET_M11, "147", 2),           // Lava Axe
            new DeckEntry(CardSet.SET_SOM, "97", 1),    // Melt Terrain
            // new DeckEntry(CardSet.SET_M11, "70", 1),         // Preordain (not yet implemented)
            new DeckEntry(CardSet.SET_MBS, "74", 1)     // Red Sun's Zenith
            // new DeckEntry(CardSet.SET_M11, "73", 1)          // Sleep (not yet implemented)
    )),

    MBS_WHITE_GREEN_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_MBS, "154", 14),  // Forest
            new DeckEntry(CardSet.SET_MBS, "146", 11),  // Plains
            new DeckEntry(CardSet.SET_SOM, "112", 2),   // Blight Mamba
            new DeckEntry(CardSet.SET_MBS, "77", 1),    // Blightwidow
            new DeckEntry(CardSet.SET_MBS, "103", 1),   // Core Prowler
            new DeckEntry(CardSet.SET_SOM, "147", 1),   // Corpse Cur
            new DeckEntry(CardSet.SET_MBS, "120", 2),   // Phyrexian Digester
            new DeckEntry(CardSet.SET_MBS, "85", 1),    // Phyrexian Hydra
            new DeckEntry(CardSet.SET_MBS, "121", 2),   // Phyrexian Juggernaut
            new DeckEntry(CardSet.SET_MBS, "125", 2),   // Plague Myr
            new DeckEntry(CardSet.SET_MBS, "16", 2),    // Priests of Norn
            new DeckEntry(CardSet.SET_MBS, "90", 2),    // Rot Wolf
            new DeckEntry(CardSet.SET_SOM, "128", 2),   // Tangle Angler
            new DeckEntry(CardSet.SET_MBS, "17", 2),    // Tine Shrike
            new DeckEntry(CardSet.SET_MBS, "94", 1),    // Viridian Corrupter
            new DeckEntry(CardSet.SET_MBS, "105", 1),   // Decimator Web
            new DeckEntry(CardSet.SET_SOM, "214", 2),   // Trigon of Infestation
            new DeckEntry(CardSet.SET_MBS, "3", 2),     // Banishment Decree
            new DeckEntry(CardSet.SET_MBS, "4", 2),     // Choking Fumes
            // new DeckEntry(CardSet.SET_M11, "22", 1),         // Mighty Leap (not yet implemented)
            new DeckEntry(CardSet.SET_MBS, "86", 1),    // Pistus Strike
            // new DeckEntry(CardSet.SET_M11, "26", 1),         // Safe Passage (not yet implemented)
            new DeckEntry(CardSet.SET_MBS, "93", 2)     // Unnatural Predation
            // new DeckEntry(CardSet.SET_M11, "182", 2)         // Hunters' Feast (not yet implemented)
    )),

    MBS_BLUE_BLACK_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_MBS, "148", 13),  // Island
            new DeckEntry(CardSet.SET_MBS, "150", 12),  // Swamp
            new DeckEntry(CardSet.SET_M11, "44", 1),            // Armored Cancrix
            new DeckEntry(CardSet.SET_M11, "82", 1),            // Barony Vampire
            new DeckEntry(CardSet.SET_MBS, "40", 1),    // Caustic Hound
            new DeckEntry(CardSet.SET_SOM, "63", 2),    // Fume Spitter
            new DeckEntry(CardSet.SET_MBS, "116", 2),   // Myr Sire
            new DeckEntry(CardSet.SET_MBS, "29", 3),    // Oculus
            new DeckEntry(CardSet.SET_MBS, "51", 2),    // Phyrexian Rager
            new DeckEntry(CardSet.SET_MBS, "123", 2),   // Pierce Strider
            new DeckEntry(CardSet.SET_MBS, "126", 1),   // Psychosis Crawler
            new DeckEntry(CardSet.SET_SOM, "78", 1),    // Skinrender
            new DeckEntry(CardSet.SET_MBS, "36", 1),    // Vedalken Anatomist
            new DeckEntry(CardSet.SET_MBS, "100", 1),   // Bonehoard
            new DeckEntry(CardSet.SET_SOM, "144", 2),   // Contagion Clasp
            new DeckEntry(CardSet.SET_MBS, "107", 1),   // Flayer Husk
            new DeckEntry(CardSet.SET_MBS, "133", 2),   // Skinwing
            new DeckEntry(CardSet.SET_SOM, "213", 2),   // Trigon of Corruption
            new DeckEntry(CardSet.SET_MBS, "137", 1),   // Strandwalker
            new DeckEntry(CardSet.SET_M11, "67", 1),            // Mind Control
            new DeckEntry(CardSet.SET_M11, "95", 1),            // Doom Blade
            new DeckEntry(CardSet.SET_MBS, "33", 1),    // Steel Sabotage
            new DeckEntry(CardSet.SET_SOM, "45", 1),    // Steady Progress
            new DeckEntry(CardSet.SET_M11, "94", 1),            // Disentomb
            new DeckEntry(CardSet.SET_MBS, "45", 1),    // Horrifying Revelation
            new DeckEntry(CardSet.SET_MBS, "56", 1),    // Spread the Sickness
            new DeckEntry(CardSet.SET_MBS, "38", 2)     // Vivisection
    )),

    MBS_BLUE_BLACK_EVENT_DECK(List.of(
            // new DeckEntry(CardSet.SET_M11, "224", 2),              // Drowned Catacomb (not yet implemented)
            new DeckEntry(CardSet.SET_MBS, "148", 10),         // Island
            // new DeckEntry(CardSet.ZENDIKAR, "215", 4),                // Jwar Isle Refuge (set not available)
            new DeckEntry(CardSet.SET_MBS, "150", 7),          // Swamp
            new DeckEntry(CardSet.SET_SOM, "147", 4),          // Corpse Cur
            new DeckEntry(CardSet.SET_SOM, "66", 1),           // Hand of the Praetors
            new DeckEntry(CardSet.SET_SOM, "185", 4),          // Necropede
            new DeckEntry(CardSet.SET_MBS, "52", 2),           // Phyrexian Vatmother
            new DeckEntry(CardSet.SET_MBS, "125", 4),          // Plague Myr
            new DeckEntry(CardSet.SET_SOM, "144", 2),          // Contagion Clasp
            new DeckEntry(CardSet.SET_MBS, "22", 4),           // Corrupted Conscience
            // new DeckEntry(CardSet.RISE_OF_THE_ELDRAZI, "59", 2),      // Deprive (set not available)
            new DeckEntry(CardSet.SET_M11, "95", 1)                     // Doom Blade
            // new DeckEntry(CardSet.SET_M11, "62", 2),               // Mana Leak (not yet implemented)
            // new DeckEntry(CardSet.WORLDWAKE, "68", 2),                // Smother (set not available)
            // new DeckEntry(CardSet.RISE_OF_THE_ELDRAZI, "101", 2),     // Consuming Vapors (set not available)
            // new DeckEntry(CardSet.SET_M11, "54", 4),               // Foresee (not yet implemented)
            // new DeckEntry(CardSet.SET_M11, "70", 3)                // Preordain (not yet implemented)
    ), List.of(
            new DeckEntry(CardSet.SET_M11, "91", 3),                   // Deathmark
            new DeckEntry(CardSet.SET_M11, "95", 1),                   // Doom Blade
            new DeckEntry(CardSet.SET_M11, "53", 3),                   // Flashfreeze
            new DeckEntry(CardSet.SET_MBS, "43", 2)            // Go for the Throat
            // new DeckEntry(CardSet.SET_M11, "68", 4),               // Negate (not yet implemented)
            // new DeckEntry(CardSet.WORLDWAKE, "68", 2)                 // Smother (set not available)
    )),

    MBS_RED_EVENT_DECK(List.of(
            new DeckEntry(CardSet.SET_MBS, "144", 1),    // Contested War Zone
            new DeckEntry(CardSet.SET_MBS, "152", 21),   // Mountain
            // new DeckEntry(CardSet.ZENDIKAR, "125", 2),           // Goblin Bushwhacker (not yet implemented)
            // new DeckEntry(CardSet.ZENDIKAR, "126", 2),           // Goblin Guide (not yet implemented)
            new DeckEntry(CardSet.SET_MBS, "64", 4),    // Goblin Wardriver
            new DeckEntry(CardSet.SET_SOM, "168", 1),   // Iron Myr
            new DeckEntry(CardSet.SET_SOM, "174", 4),   // Memnite
            new DeckEntry(CardSet.SET_10E, "336", 4),        // Ornithopter
            new DeckEntry(CardSet.SET_MBS, "131", 4),   // Signal Pest
            new DeckEntry(CardSet.SET_SOM, "104", 1),   // Spikeshot Elder
            new DeckEntry(CardSet.SET_SOM, "149", 2),   // Darksteel Axe
            new DeckEntry(CardSet.SET_SOM, "191", 2),   // Panic Spellbomb
            new DeckEntry(CardSet.SET_SOM, "91", 2),    // Galvanic Blast
            // new DeckEntry(CardSet.SET_M11, "149", 4),         // Lightning Bolt (not yet implemented)
            // new DeckEntry(CardSet.RISE_OF_THE_ELDRAZI, "140", 2), // Devastating Summons (not yet implemented)
            new DeckEntry(CardSet.SET_SOM, "96", 4)     // Kuldotha Rebirth
    ), List.of(
            new DeckEntry(CardSet.SET_M11, "121", 2),           // Act of Treason
            // new DeckEntry(CardSet.ZENDIKAR, "127", 4),           // Goblin Ruinblaster (not yet implemented)
            new DeckEntry(CardSet.SET_MBS, "67", 2)     // Into the Core
            // new DeckEntry(CardSet.SET_M11, "148", 1),         // Leyline of Punishment (not yet implemented)
            // new DeckEntry(CardSet.WORLDWAKE, "90", 4),           // Searing Blaze (not yet implemented)
            // new DeckEntry(CardSet.ZENDIKAR, "153", 2)            // Unstable Footing (not yet implemented)
    )),

    NPH_WHITE_GREEN_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_NPH, "174", 12),         // Forest
            new DeckEntry(CardSet.SET_NPH, "166", 12),         // Plains
            new DeckEntry(CardSet.SET_NPH, "4", 1),            // Blade Splicer
            new DeckEntry(CardSet.SET_NPH, "105", 1),          // Brutalizer Exarch
            new DeckEntry(CardSet.SET_SOM, "146", 3),     // Copper Myr
            // new DeckEntry(CardSet.SET_M11, "177", 1),          // Garruk's Packleader (not yet implemented)
            new DeckEntry(CardSet.SET_SOM, "157", 3),     // Gold Myr
            new DeckEntry(CardSet.SET_SOM, "159", 1),     // Golem Artisan
            new DeckEntry(CardSet.SET_NPH, "16", 3),           // Master Splicer
            new DeckEntry(CardSet.SET_NPH, "114", 1),          // Maul Splicer
            new DeckEntry(CardSet.SET_SOM, "190", 2),     // Palladium Myr
            new DeckEntry(CardSet.SET_NPH, "150", 1),          // Phyrexian Hulk
            new DeckEntry(CardSet.SET_SOM, "194", 1),     // Precursor Golem
            new DeckEntry(CardSet.SET_NPH, "22", 2),           // Sensor Splicer
            // new DeckEntry(CardSet.SET_M11, "215", 1),          // Stone Golem (not yet implemented)
            new DeckEntry(CardSet.SET_NPH, "25", 2),           // Suture Priest
            new DeckEntry(CardSet.SET_NPH, "126", 2),          // Vital Splicer
            new DeckEntry(CardSet.SET_NPH, "133", 1),          // Conversion Chamber
            new DeckEntry(CardSet.SET_NPH, "11", 2),           // Forced Worship
            new DeckEntry(CardSet.SET_NPH, "125", 1),          // Viridian Harvest
            new DeckEntry(CardSet.SET_M11, "178", 1),            // Giant Growth
            new DeckEntry(CardSet.SET_NPH, "110", 1),          // Glissa's Scorn
            // new DeckEntry(CardSet.SET_M11, "22", 1),           // Mighty Leap (not yet implemented)
            new DeckEntry(CardSet.SET_NPH, "26", 2)              // War Report
            // new DeckEntry(CardSet.SET_M11, "168", 2)           // Cultivate (not yet implemented)
    )),

    NPH_BLUE_BLACK_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_NPH, "168", 13),         // Island
            new DeckEntry(CardSet.SET_NPH, "170", 11),         // Swamp
            new DeckEntry(CardSet.SET_M11, "45", 2),             // Augury Owl
            new DeckEntry(CardSet.SET_NPH, "52", 2),           // Blind Zealot
            new DeckEntry(CardSet.SET_MBS, "101", 2),     // Brass Squire
            new DeckEntry(CardSet.SET_SOM, "30", 1),      // Darkslick Drake
            new DeckEntry(CardSet.SET_NPH, "55", 2),           // Dementia Bat
            new DeckEntry(CardSet.SET_NPH, "138", 3),          // Hovermyr
            new DeckEntry(CardSet.SET_NPH, "36", 2),           // Impaler Shrike
            new DeckEntry(CardSet.SET_NPH, "142", 1),          // Kiln Walker
            new DeckEntry(CardSet.SET_NPH, "66", 2),           // Mortis Dogs
            new DeckEntry(CardSet.SET_SOM, "71", 1),      // Necrogen Scudder
            new DeckEntry(CardSet.SET_SOM, "37", 1),      // Neurok Invisimancer
            new DeckEntry(CardSet.SET_NPH, "41", 1),           // Phyrexian Ingester
            new DeckEntry(CardSet.SET_SOM, "202", 1),     // Silver Myr
            new DeckEntry(CardSet.SET_NPH, "46", 2),           // Spire Monitor
            new DeckEntry(CardSet.SET_SOM, "137", 1),     // Argentum Armor
            new DeckEntry(CardSet.SET_MBS, "102", 2),     // Copper Carapace
            new DeckEntry(CardSet.SET_NPH, "147", 1),          // Necropouncer
            new DeckEntry(CardSet.SET_NPH, "157", 2),          // Sickleslicer
            new DeckEntry(CardSet.SET_MBS, "143", 2),     // Viridian Claw
            // new DeckEntry(CardSet.MAGIC_2012, "221", 2),          // Warlord's Axe (not yet implemented)
            new DeckEntry(CardSet.SET_M11, "95", 1),             // Doom Blade
            new DeckEntry(CardSet.SET_NPH, "48", 2)            // Vapor Snag
    )),

    NPH_WHITE_RED_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_NPH, "172", 11),           // Mountain
            new DeckEntry(CardSet.SET_NPH, "166", 13),           // Plains
            new DeckEntry(CardSet.SET_NPH, "131", 2),            // Blinding Souleater
            new DeckEntry(CardSet.SET_NPH, "5", 1),              // Cathedral Membrane
            new DeckEntry(CardSet.SET_NPH, "139", 2),            // Immolating Souleater
            new DeckEntry(CardSet.SET_NPH, "12", 1),             // Inquisitor Exarch
            new DeckEntry(CardSet.SET_SOM, "13", 2),        // Kemba's Skyguard
            new DeckEntry(CardSet.SET_MBS, "112", 1),       // Lumengrid Gargoyle
            new DeckEntry(CardSet.SET_NPH, "88", 1),             // Moltensteel Dragon
            new DeckEntry(CardSet.SET_MBS, "72", 1),        // Ogre Resister
            new DeckEntry(CardSet.SET_NPH, "19", 3),             // Porcelain Legionnaire
            new DeckEntry(CardSet.SET_NPH, "23", 1),             // Shattered Angel
            new DeckEntry(CardSet.SET_NPH, "96", 3),             // Slash Panther
            new DeckEntry(CardSet.SET_SOM, "161", 2),       // Golem's Heart
            new DeckEntry(CardSet.SET_NPH, "151", 1),            // Pristine Talisman
            new DeckEntry(CardSet.SET_NPH, "91", 2),             // Rage Extractor
            new DeckEntry(CardSet.SET_M11, "23", 2),               // Pacifism
            new DeckEntry(CardSet.SET_NPH, "78", 2),             // Act of Aggression
            new DeckEntry(CardSet.SET_NPH, "2", 1),              // Apostle's Blessing
            new DeckEntry(CardSet.SET_NPH, "86", 1),             // Gut Shot
            // new DeckEntry(CardSet.SET_M11, "145", 1),           // Incite (not yet implemented)
            // new DeckEntry(CardSet.SET_M11, "149", 1),           // Lightning Bolt (not yet implemented)
            new DeckEntry(CardSet.SET_NPH, "15", 1),             // Marrow Shards
            new DeckEntry(CardSet.SET_SOM, "27", 2),        // Whitesun's Passage
            new DeckEntry(CardSet.SET_MBS, "15", 1)         // Phyrexian Rebirth
            // new DeckEntry(CardSet.SET_M11, "32", 1)             // Solemn Offering (not yet implemented)
    )),

    NPH_BLACK_RED_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_NPH, "172", 11),            // Mountain
            new DeckEntry(CardSet.SET_NPH, "170", 13),            // Swamp
            new DeckEntry(CardSet.SET_SOM, "56", 2),         // Blistergrub
            new DeckEntry(CardSet.SET_MBS, "58", 2),         // Blisterstick Shaman
            new DeckEntry(CardSet.SET_MBS, "40", 2),         // Caustic Hound
            new DeckEntry(CardSet.SET_NPH, "54", 1),              // Chancellor of the Dross
            new DeckEntry(CardSet.SET_NPH, "59", 2),              // Entomber Exarch
            new DeckEntry(CardSet.SET_NPH, "83", 2),              // Flameborn Viron
            new DeckEntry(CardSet.SET_NPH, "84", 2),              // Furnace Scamp
            new DeckEntry(CardSet.SET_MBS, "51", 2),         // Phyrexian Rager
            new DeckEntry(CardSet.SET_M11, "152", 1),               // Prodigal Pyromancer
            new DeckEntry(CardSet.SET_SOM, "102", 1),        // Scoria Elemental
            new DeckEntry(CardSet.SET_NPH, "97", 2),              // Tormentor Exarch
            new DeckEntry(CardSet.SET_NPH, "153", 2),             // Shrine of Burning Rage
            new DeckEntry(CardSet.SET_SOM, "212", 1),        // Tower of Calamities
            new DeckEntry(CardSet.SET_NPH, "58", 1),              // Enslave
            new DeckEntry(CardSet.SET_NPH, "67", 2),              // Parasitic Implant
            new DeckEntry(CardSet.SET_NPH, "79", 2),              // Artillerize
            // new DeckEntry(CardSet.SET_M11, "140", 1),            // Fling (not yet implemented)
            new DeckEntry(CardSet.SET_NPH, "61", 2),              // Geth's Verdict
            new DeckEntry(CardSet.SET_MBS, "43", 1),         // Go for the Throat
            new DeckEntry(CardSet.SET_NPH, "56", 1),              // Despise
            // new DeckEntry(CardSet.SET_M11, "94", 1),             // Disentomb (not yet implemented)
            new DeckEntry(CardSet.SET_NPH, "64", 1),              // Ichor Explosion
            new DeckEntry(CardSet.SET_MBS, "47", 1),         // Morbid Plunder
            new DeckEntry(CardSet.SET_NPH, "102", 1)              // Whipflare
    )),

    NPH_BLUE_GREEN_THEME_DECK(List.of(
            new DeckEntry(CardSet.SET_NPH, "174", 12),              // Forest
            new DeckEntry(CardSet.SET_NPH, "168", 12),              // Island
            new DeckEntry(CardSet.SET_SOM, "112", 1),          // Blight Mamba
            new DeckEntry(CardSet.SET_NPH, "29", 3),                // Blighted Agent
            new DeckEntry(CardSet.SET_NPH, "30", 2),                // Chained Throatseeker
            new DeckEntry(CardSet.SET_MBS, "103", 1),          // Core Prowler
            new DeckEntry(CardSet.SET_SOM, "117", 2),          // Cystbearer
            new DeckEntry(CardSet.SET_NPH, "111", 3),               // Glistener Elf
            new DeckEntry(CardSet.SET_NPH, "117", 2),               // Mycosynth Fiend
            new DeckEntry(CardSet.SET_NPH, "119", 1),               // Phyrexian Swarmlord
            new DeckEntry(CardSet.SET_MBS, "87", 1),           // Plaguemaw Beast
            new DeckEntry(CardSet.SET_MBS, "90", 1),           // Rot Wolf
            new DeckEntry(CardSet.SET_NPH, "121", 1),               // Spinebiter
            new DeckEntry(CardSet.SET_NPH, "49", 2),                // Viral Drake
            new DeckEntry(CardSet.SET_NPH, "124", 2),               // Viridian Betrayers
            new DeckEntry(CardSet.SET_SOM, "222", 1),          // Wall of Tanglecord
            new DeckEntry(CardSet.SET_SOM, "144", 1),          // Contagion Clasp
            new DeckEntry(CardSet.SET_SOM, "214", 2),          // Trigon of Infestation
            new DeckEntry(CardSet.SET_MBS, "22", 1),           // Corrupted Conscience
            new DeckEntry(CardSet.SET_NPH, "34", 1),                // Defensive Stance
            new DeckEntry(CardSet.SET_SOM, "35", 1),           // Inexorable Tide
            new DeckEntry(CardSet.SET_NPH, "32", 1),                // Corrupted Resolve
            new DeckEntry(CardSet.SET_MBS, "25", 1),           // Fuel for the Cause
            new DeckEntry(CardSet.SET_NPH, "113", 3),               // Leeching Bite
            new DeckEntry(CardSet.SET_10E, "282", 1),              // Naturalize
            new DeckEntry(CardSet.SET_SOM, "45", 1)            // Steady Progress
    )),

    NPH_GREEN_EVENT_DECK(List.of(
            new DeckEntry(CardSet.SET_NPH, "174", 22),            // Forest
            new DeckEntry(CardSet.SET_MBS, "145", 1),        // Inkmoth Nexus
            new DeckEntry(CardSet.SET_SOM, "112", 1),        // Blight Mamba
            new DeckEntry(CardSet.SET_NPH, "111", 4),             // Glistener Elf
            new DeckEntry(CardSet.SET_SOM, "166", 2),        // Ichorclaw Myr
            // new DeckEntry(CardSet.RISE_OF_THE_ELDRAZI, "203", 4),   // Overgrown Battlement (set not available)
            new DeckEntry(CardSet.SET_SOM, "126", 2),        // Putrefax
            new DeckEntry(CardSet.SET_MBS, "90", 3),         // Rot Wolf
            new DeckEntry(CardSet.SET_MBS, "94", 3),         // Viridian Corrupter
            new DeckEntry(CardSet.SET_SOM, "144", 1),        // Contagion Clasp
            new DeckEntry(CardSet.SET_SOM, "115", 4),        // Carrion Call
            // new DeckEntry(CardSet.WORLDWAKE, "104", 4),              // Groundswell (set not available)
            new DeckEntry(CardSet.SET_NPH, "116", 4),             // Mutagenic Growth
            // new DeckEntry(CardSet.ZENDIKAR, "176", 4),               // Primal Bellow (set not available)
            new DeckEntry(CardSet.SET_MBS, "81", 1)          // Green Sun's Zenith
    ), List.of(
            new DeckEntry(CardSet.SET_SOM, "144", 3),        // Contagion Clasp
            new DeckEntry(CardSet.SET_NPH, "115", 1),             // Melira, Sylvok Outcast
            // new DeckEntry(CardSet.SET_M11, "188", 2),             // Obstinate Baloth (not yet implemented)
            new DeckEntry(CardSet.SET_MBS, "86", 1),         // Pistus Strike
            new DeckEntry(CardSet.SET_SOM, "214", 3),        // Trigon of Infestation
            new DeckEntry(CardSet.SET_MBS, "93", 2),         // Unnatural Predation
            // new DeckEntry(CardSet.ZENDIKAR, "193", 2),               // Vines of Vastwood (set not available)
            new DeckEntry(CardSet.SET_MBS, "94", 1)          // Viridian Corrupter
    )),

    NPH_WHITE_EVENT_DECK(List.of(
            // new DeckEntry(CardSet.WORLDWAKE, "161", 2),                 // Dread Statuary (set not available)
            new DeckEntry(CardSet.SET_NPH, "166", 21),              // Plains
            new DeckEntry(CardSet.SET_M11, "13", 4),                  // Elite Vanguard
            new DeckEntry(CardSet.SET_SOM, "12", 1),           // Kemba, Kha Regent
            // new DeckEntry(CardSet.ZENDIKAR, "10", 2),                   // Kor Duelist (set not available)
            new DeckEntry(CardSet.SET_MBS, "10", 4),           // Leonin Relic-Warder
            new DeckEntry(CardSet.SET_MBS, "11", 4),           // Leonin Skyhunter
            new DeckEntry(CardSet.SET_MBS, "14", 1),           // Mirran Crusader
            new DeckEntry(CardSet.SET_NPH, "19", 4),                // Porcelain Legionnaire
            new DeckEntry(CardSet.SET_NPH, "20", 1),                // Puresteel Paladin
            // new DeckEntry(CardSet.WORLDWAKE, "20", 2),                  // Stoneforge Mystic (set not available)
            new DeckEntry(CardSet.SET_MBS, "100", 1),          // Bonehoard
            new DeckEntry(CardSet.SET_SOM, "149", 1),          // Darksteel Axe
            new DeckEntry(CardSet.SET_MBS, "107", 4),          // Flayer Husk
            new DeckEntry(CardSet.SET_NPH, "157", 1),               // Sickleslicer
            new DeckEntry(CardSet.SET_MBS, "133", 1),          // Skinwing
            // new DeckEntry(CardSet.SET_M11, "216", 1),                // Sword of Vengeance (not yet implemented)
            // new DeckEntry(CardSet.ZENDIKAR, "14", 4),                   // Journey to Nowhere (set not available)
            new DeckEntry(CardSet.SET_NPH, "2", 1)                  // Apostle's Blessing
    ), List.of(
            new DeckEntry(CardSet.SET_NPH, "2", 1),                 // Apostle's Blessing
            new DeckEntry(CardSet.SET_SOM, "2", 2),            // Arrest
            new DeckEntry(CardSet.SET_M11, "9", 3),                   // Celestial Purge
            // new DeckEntry(CardSet.ZENDIKAR, "10", 1),                   // Kor Duelist (set not available)
            // new DeckEntry(CardSet.WORLDWAKE, "11", 4),                  // Kor Firewalker (set not available)
            new DeckEntry(CardSet.SET_SOM, "18", 4)            // Revoke Existence
    ));

    public record DeckEntry(CardSet cardSet, String collectorNumber, int count) {}

    private final String id;
    private final String displayName;
    private final List<DeckEntry> entries;
    private final List<DeckEntry> sideboard;

    PrebuiltDeck(List<DeckEntry> entries) {
        this(entries, List.of());
    }

    PrebuiltDeck(List<DeckEntry> entries, List<DeckEntry> sideboard) {
        String[] tokens = name().replaceFirst("^_", "").split("_");
        this.id = String.join("-", tokens).toLowerCase(Locale.ROOT);
        StringBuilder display = new StringBuilder(tokens[0]);
        for (int i = 1; i < tokens.length; i++) {
            display.append(' ').append(tokens[i].charAt(0)).append(tokens[i].substring(1).toLowerCase(Locale.ROOT));
        }
        this.displayName = display.toString();
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
