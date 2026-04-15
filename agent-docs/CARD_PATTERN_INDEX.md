# CARD_PATTERN_INDEX

Purpose: quickly find a reference card for the pattern you're implementing. One or two examples per archetype. All paths relative to `cards/`.

This index has been split into smaller files for faster lookup. Each file is under 10k tokens.

## File index

| File | Sections | When to use |
|------|----------|-------------|
| [CARD_PATTERNS_LANDS_SPELLS.md](CARD_PATTERNS_LANDS_SPELLS.md) | Lands, Spells | Implementing lands (basic, pain, check, fast, creature, utility) or spells (burn, pump, destroy, board wipe, draw, mill, counterspell, modal, graveyard, steal, extra turn) |
| [CARD_PATTERNS_CREATURES_ETB.md](CARD_PATTERNS_CREATURES_ETB.md) | Vanilla, Keyword, ETB creatures | Implementing creatures with no abilities, keyword-only creatures, or ETB triggers |
| [CARD_PATTERNS_CREATURES_TRIGGERED.md](CARD_PATTERNS_CREATURES_TRIGGERED.md) | Triggered creatures | Implementing creatures with triggered abilities (attack, block, death, damage, upkeep, draw, spell cast, graveyard) |
| [CARD_PATTERNS_PERMANENTS_STATIC.md](CARD_PATTERNS_PERMANENTS_STATIC.md) | Static permanents, Auras | Implementing lords/anthems, static restrictions, auras (lockdown, boost, curse) |
| [CARD_PATTERNS_PERMANENTS_ARTIFACTS.md](CARD_PATTERNS_PERMANENTS_ARTIFACTS.md) | Artifacts, Vehicles, Equipment | Implementing artifacts, vehicles, or equipment |
| [CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md](CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md) | Activated abilities, Planeswalkers, Sagas | Implementing activated abilities (tap, sacrifice, mana, pump), planeswalkers, or sagas |
| [CARD_COPY_PASTE_TEMPLATES.md](CARD_COPY_PASTE_TEMPLATES.md) | Full card + test templates | Ready-to-use copy-paste templates for the most common archetypes (pump, burn, draw, destroy, ETB, counter, aura, aura with ability). Use line offsets from its quick selector table. |

## Quick pattern-to-file lookup

| Pattern keyword | File |
|----------------|------|
| land, basic, pain, check, fast, manland | CARD_PATTERNS_LANDS_SPELLS.md |
| burn, damage, shock, bolt, X burn | CARD_PATTERNS_LANDS_SPELLS.md |
| pump, boost, giant growth, overrun | CARD_PATTERNS_LANDS_SPELLS.md |
| destroy, terror, wrath, board wipe | CARD_PATTERNS_LANDS_SPELLS.md |
| draw, mill, discard, tutor, search | CARD_PATTERNS_LANDS_SPELLS.md |
| counter, counterspell, cancel | CARD_PATTERNS_LANDS_SPELLS.md |
| bounce, unsummon, return to hand | CARD_PATTERNS_LANDS_SPELLS.md |
| graveyard return, reanimate, flashback | CARD_PATTERNS_LANDS_SPELLS.md |
| modal, choose one, fight, bite | CARD_PATTERNS_LANDS_SPELLS.md |
| steal, threaten, extra turn | CARD_PATTERNS_LANDS_SPELLS.md |
| vanilla, no abilities, empty body | CARD_PATTERNS_CREATURES_ETB.md |
| keyword creature, flying, haste, infect | CARD_PATTERNS_CREATURES_ETB.md |
| ETB, enters the battlefield | CARD_PATTERNS_CREATURES_ETB.md |
| kicker, alternate casting cost | CARD_PATTERNS_CREATURES_ETB.md |
| attack trigger, death trigger, upkeep trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| combat damage trigger, block trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| graveyard trigger, graveyard ability | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| spell cast trigger, opponent spell | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| landfall, land enters trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| lord, anthem, static boost | CARD_PATTERNS_PERMANENTS_STATIC.md |
| aura, enchant creature, pacifism | CARD_PATTERNS_PERMANENTS_STATIC.md |
| curse, enchant player | CARD_PATTERNS_PERMANENTS_STATIC.md |
| metalcraft, morbid, conditional | CARD_PATTERNS_PERMANENTS_STATIC.md |
| artifact, charge counter, spellbomb | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| vehicle, crew | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| equipment, equip, living weapon | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| activated ability, tap ability, sacrifice ability | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| mana ability, mana dork | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| planeswalker, loyalty | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| saga, chapter, lore counter | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| template, copy-paste, skeleton | CARD_COPY_PASTE_TEMPLATES.md |

## Canonical test reference per pattern

When implementing a card, use these as the **best** test file to read for each common pattern:

| Pattern | Best test reference | Why |
|---------|-------------------|-----|
| Aura with static boost (+X/+Y or -X/-Y) | `SensoryDeprivationTest.java` | Covers casting, resolution, stat check, removal, fizzle, targeting |
| Aura lockdown (can't attack/block) | `PacifismTest.java` | Covers combat restriction + removal |
| Simple burn spell | `ShockTest.java` | Covers creature + player targeting + fizzle |
| Non-targeted pump | `ChargeTest.java` | Covers boost + opponent unaffected + cleanup reset |
| ETB creature (non-targeted) | `AngelOfMercyTest.java` | Covers ETB trigger resolution |
| ETB creature (targeted) | `BriarpackAlphaTest.java` | Covers targeted ETB + fizzle + flash |
| Counterspell | `CancelTest.java` | Covers counter + graveyard |
| Draw spell | `CounselOfTheSoratamiTest.java` | Covers draw count + graveyard |
| Destroy spell | `TerrorTest.java` | Covers destroy + filter + fizzle |
| Equipment | `LeoninScimitarTest.java` | Covers equip + boost + unequip |
| Lord/anthem | `GloriousAnthemTest.java` | Covers static boost + removal |
| Vanilla creature | (no test needed) | Empty body, no engine logic |
