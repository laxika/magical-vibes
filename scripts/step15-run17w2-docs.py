#!/usr/bin/env python3
"""Step 15, run 17w2: agent-docs for the cast triggers that read the triggering player and spell.

Line rewrites in EFFECTS_QUICK_REFERENCE.md, EFFECTS_INDEX.md, ORACLE_TEXT_EFFECT_MAP.md,
CARD_PATTERNS_CREATURES_TRIGGERED.md, PREDICATES_REFERENCE.md and ARCHITECTURE.md: the rows of the twelve deleted
trigger-condition records go or name the existing effects the cards now use, the new predicate and
`BecomeCreatureEffect`'s amount form are documented. A line is found by a unique marker; a line already rewritten is
left alone. Idempotent. Fails if a doc or skill still names a deleted class.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
SKILLS = ROOT / ".claude/skills"

DELETED = ["CasterLosesLifeOnSpellCastEffect", "CasterLosesLifeOnChosenColorSpellCastEffect", "SpellCastLifeDrainEffect",
           "SpellCastDamageToCasterEffect", "CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect",
           "DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect",
           "GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect",
           "DiscardForSameNameCardsInGraveyardsOnSpellCastEffect", "DiscardAllCardsWithCastSpellManaValueEffect",
           "DestroyAllPermanentsWithCastSpellManaValueEffect", "BecomeCreatureByCastSpellManaValueEffect",
           "BoostAllOwnCreaturesByColorsSpentOnSpellCastEffect"]

SAME_NAME = "`CardsInGraveyard(new CardSharesNameWithTriggeringSpellPredicate(), CountScope.ANY_PLAYER)`"

# file -> [(marker in the line to replace, new line or None to delete)]
EDITS = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `CasterLosesLifeOnChosenColorSpellCastEffect(int amount)`", None),
        ("- `BoostAllOwnCreaturesByColorsSpentOnSpellCastEffect(", None),
        ("- `DiscardAllCardsWithCastSpellManaValueEffect(", None),
        ("- `DestroyAllPermanentsWithCastSpellManaValueEffect(", None),
        ("- `CasterLosesLifeOnSpellCastEffect(CardPredicate spellFilter, int amount)`",
         "- \"Whenever a player casts a [filter] spell, that player loses N life\" — `Triggers.playerCastsSpell().withFilter(filter)` "
         "+ `LoseLifeEffect(N, LoseLifeRecipient.TRIGGERING_PLAYER)`: the cast collector gives the entry the caster as its "
         "non-targeting \"that player\" (Soot Imp, Curse of Wizardry with `CardHasSourceChosenColorPredicate`); \"that player "
         "loses N life and you gain M life\" adds `GainLifeEffect(M)` to the same ability (Yawgmoth's Edict)"),
        ("- `CasterLosesLifeOnSpellCastEffect(CardPredicate spellFilter, int amount[, boolean useSpellManaValue])`",
         "- \"Whenever you cast a spell, you lose life equal to its mana value\" — `Triggers.youCastSpell().anySpell()` + "
         "`LoseLifeEffect(new TargetSpellManaValue(), LoseLifeRecipient.CONTROLLER)` (Teval, Arbiter of Virtue)"),
        ("- `GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect()`",
         "- \"... for each card in all graveyards with the same name as that spell\" — " + SAME_NAME + ", read as the "
         "ability resolves on a `Triggers.playerCastsSpell().anySpell()` ability, with a `TRIGGERING_PLAYER` recipient: "
         "`GainLifeEffect(..., GainLifeRecipient.TRIGGERING_PLAYER)` (Aven Shrine), `DiscardEffect(..., "
         "DiscardRecipient.TRIGGERING_PLAYER)` (Cabal Shrine), `DealDamageToPlayersEffect(new Scaled(..., 2), "
         "DamageRecipient.TRIGGERING_PLAYER)` (Dwarven Shrine), `CreateTokenForTriggeringPlayerEffect(new CreateTokenEffect(...))` "
         "(Nantuko Shrine)"),
        ("- `CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect()`", None),
        ("- `DiscardForSameNameCardsInGraveyardsOnSpellCastEffect()`", None),
        ("- `DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect()`", None),
        ("- `SpellCastLifeDrainEffect(", None),
        ("- `SpellCastDamageToCasterEffect(",
         "- \"Whenever an opponent casts a [filter] spell, this permanent deals N damage to that player\" — "
         "`Triggers.opponentCastsSpell().withFilter(filter)` + `DealDamageToPlayersEffect(N, DamageRecipient.TRIGGERING_PLAYER)` "
         "(Mindsparker); \"if this creature is renowned\" is `TriggerOptions.DEFAULT.withInterveningIf(new SourceIsRenowned())` "
         "(Scab-Clan Berserker); \"if it's not their turn\" is `.notDuringCastersTurn()` (Scytheclaw Raptor)"),
        ("- `BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes, Set<Keyword> keywords,",
         "- `BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes, Set<Keyword> keywords, "
         "Set<CardColor> protectionFromColors, Set<CardType> additionalTypes)` permanently changes the source into a "
         "creature with the supplied base P/T and subtypes, optional protection, and additional card types. Convenience "
         "constructors default the additional types to empty. `BecomeCreatureEffect(DynamicAmount power, DynamicAmount "
         "toughness, CardSubtype)` evaluates the base P/T once as it resolves (Veiled Sentry: `new TargetSpellManaValue()` "
         "for both)."),
        ("- `ColorsSpentToCast()` — number of distinct colors of mana spent to cast the source spell;",
         "- `ColorsSpentToCast()` — number of distinct colors of mana spent to cast the source spell; use as a "
         "`DynamicAmount` for Converge effects on spells with a separate `{X}` cost. On a cast trigger it is the colors "
         "spent on the triggering spell, which the cast collector gives the ability as its X (Magmablood Archaic's "
         "`BoostAllOwnCreaturesEffect(new ColorsSpentToCast(), new Fixed(0))`)."),
    ],
    "EFFECTS_INDEX.md": [
        ("| `CasterLosesLifeOnChosenColorSpellCastEffect` |", None),
        ("| `BoostAllOwnCreaturesByColorsSpentOnSpellCastEffect` |", None),
        ("| `DiscardAllCardsWithCastSpellManaValueEffect` |", None),
        ("| `DestroyAllPermanentsWithCastSpellManaValueEffect` |", None),
        ("| `CasterLosesLifeOnSpellCastEffect` |", None),
        ("| `GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect` |", None),
        ("| `CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect` |", None),
        ("| `DiscardForSameNameCardsInGraveyardsOnSpellCastEffect` |", None),
        ("| `DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect` |", None),
        ("| `SpellCastLifeDrainEffect` |", None),
        ("| `SpellCastDamageToCasterEffect` |", None),
        ("| `CasterLosesLifeOnSpellCastEffect.equalToSpellManaValue` |", None),
        ("- `BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes, Set<Keyword> keywords,",
         "- `BecomeCreatureEffect(int power, int toughness, List<CardSubtype> subtypes, Set<Keyword> keywords, "
         "Set<CardColor> protectionFromColors, Set<CardType> additionalTypes)` permanently changes the source into a "
         "creature with the supplied base P/T and subtypes, optional protection, and additional card types. Convenience "
         "constructors default the additional types to empty. `BecomeCreatureEffect(DynamicAmount power, DynamicAmount "
         "toughness, CardSubtype)` evaluates the base P/T once as it resolves (Veiled Sentry)."),
        ("  confused with the announced X value. Sweep the Skies uses it for its Thopter count.",
         "  confused with the announced X value. Sweep the Skies uses it for its Thopter count. On a cast trigger it is\n"
         "  the colors spent on the triggering spell, which the cast collector gives the ability as its X (Magmablood\n"
         "  Archaic)."),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `ChooseColorOnEnterEffect()` + `CasterLosesLifeOnChosenColorSpellCastEffect(N)` |",
         "| \"Whenever a player casts a spell of the chosen color, that player loses N life\" | `ChooseColorOnEnterEffect()` + "
         "`LoseLifeEffect(N, LoseLifeRecipient.TRIGGERING_PLAYER)` | STATIC + `Triggers.playerCastsSpell().withFilter(new "
         "CardHasSourceChosenColorPredicate())` | Curse of Wizardry. A multicolored spell containing the chosen color "
         "matches (its colors as effects leave them); the cast collector gives the entry the caster as \"that player\" |"),
        ("| `BoostAllOwnCreaturesByColorsSpentOnSpellCastEffect(CardAnyOfPredicate(INSTANT, SORCERY))` |",
         "| \"Whenever you cast an instant or sorcery spell, creatures you control get +1/+0 until end of turn for each "
         "color of mana spent to cast that spell\" | `BoostAllOwnCreaturesEffect(new ColorsSpentToCast(), new Fixed(0))` | "
         "`Triggers.youCastSpell().withFilter(CardAnyOfPredicate(INSTANT, SORCERY))` | Magmablood Archaic; the cast "
         "collector gives the ability the colors spent on the spell as its X, which `ColorsSpentToCast` reads |"),
        ("| `CasterLosesLifeOnSpellCastEffect(spellFilter, N)` |",
         "| \"Whenever a player casts a [filter] spell, that player loses N life\" | `LoseLifeEffect(N, "
         "LoseLifeRecipient.TRIGGERING_PLAYER)` | `Triggers.playerCastsSpell().withFilter(spellFilter)` | Soot Imp "
         "(nonblack = `new CardNotPredicate(new CardColorPredicate(BLACK))`, N=1). The cast collector gives the entry the "
         "caster as its non-targeting \"that player\". Opponents only: `Triggers.opponentCastsSpell()` (Havoc, Mage Hunter) |"),
        ("| `CasterLosesLifeOnSpellCastEffect.equalToSpellManaValue(null)` |",
         "| \"Whenever you cast a spell, you lose life equal to its mana value\" | `LoseLifeEffect(new TargetSpellManaValue(), "
         "LoseLifeRecipient.CONTROLLER)` | `Triggers.youCastSpell().anySpell()` | Teval, Arbiter of Virtue; the spell's "
         "mana value as the ability resolves, as it last existed if it left the stack |"),
        ("| `DestroyAllPermanentsWithCastSpellManaValueEffect(",
         "| \"Whenever you cast a Spirit or Arcane spell, destroy all permanents with that spell's mana value\" | "
         "`DestroyAllPermanentsEffect(new PermanentManaValueEqualsXPredicate())` | `Triggers.youCastSpell().withFilter(new "
         "CardAnyOfPredicate(List.of(new CardSubtypePredicate(SPIRIT), new CardSubtypePredicate(ARCANE))))` | Celestial "
         "Kirin. An effect filter that reads X on a cast trigger gets the spell's mana value (with its chosen X) as the "
         "ability's X |"),
        ("| `DiscardAllCardsWithCastSpellManaValueEffect(",
         "| \"Whenever you cast a Spirit or Arcane spell, target player reveals their hand and discards all cards with that "
         "spell's mana value\" | `RevealHandAndDiscardMatchingCardsEffect(new CardManaValueEqualsXPredicate())` | "
         "`Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(new CardSubtypePredicate(SPIRIT), new "
         "CardSubtypePredicate(ARCANE))))` | Infernal Kirin. The target choice and the entry get the spell's mana value as "
         "the ability's X |"),
        ("| `DiscardForSameNameCardsInGraveyardsOnSpellCastEffect()` |",
         "| \"Whenever a player casts a spell, that player discards X cards, where X is the number of cards in all graveyards "
         "with the same name as that spell\" | `DiscardEffect(" + SAME_NAME[1:-1] + ", DiscardRecipient.TRIGGERING_PLAYER)` | "
         "`Triggers.playerCastsSpell().anySpell()` | Cabal Shrine. The name is the triggering spell's as the ability "
         "resolves (its snapshot if it left the stack) |"),
        ("| `SpellCastLifeDrainEffect(N, M, new CardColorPredicate(WHITE))` |",
         "| \"Whenever an opponent casts a [color/filtered] spell, that player loses N life and you gain M life\" | "
         "`LoseLifeEffect(N, LoseLifeRecipient.TRIGGERING_PLAYER)` + `GainLifeEffect(M)` | "
         "`Triggers.opponentCastsSpell().withFilter(new CardColorPredicate(WHITE))` | Yawgmoth's Edict (1, 1, white), "
         "Kambal (2, 2, noncreature); Havoc only loses (no `GainLifeEffect`) |"),
        ("| `SpellCastDamageToCasterEffect(N, CardAllOfPredicate(",
         "| \"Whenever an opponent casts a [color/type filtered] spell, this creature deals N damage to that player\" | "
         "`DealDamageToPlayersEffect(N, DamageRecipient.TRIGGERING_PLAYER)` | `Triggers.opponentCastsSpell().withFilter("
         "CardAllOfPredicate(List.of(CardAnyOfPredicate(colors), CardAnyOfPredicate(types))))` | Mindsparker (2, "
         "white-or-blue instant-or-sorcery). \"if it's not their turn\": `Triggers.playerCastsSpell().notDuringCastersTurn()` "
         "(Scytheclaw Raptor) |"),
        ("| `SpellCastDamageToCasterEffect(N, CardNotPredicate(CardTypePredicate(CREATURE)), new SourceIsRenowned())` |",
         "| \"Whenever an opponent casts a noncreature spell, if this creature is renowned, this creature deals N damage to "
         "that player\" | `DealDamageToPlayersEffect(N, DamageRecipient.TRIGGERING_PLAYER)` with "
         "`TriggerOptions.DEFAULT.withInterveningIf(new SourceIsRenowned())` | `Triggers.opponentCastsSpell().withFilter("
         "CardNotPredicate(CardTypePredicate(CREATURE)))` | Scab-Clan Berserker (2). The intervening \"if\" (CR 603.4) is "
         "checked as the spell is cast and again as the ability resolves |"),
        ("| `GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect()` |",
         "| \"Whenever a player casts a spell, that player gains X life, where X is the number of cards in all graveyards "
         "with the same name as that spell\" | `GainLifeEffect(" + SAME_NAME[1:-1] + ", GainLifeRecipient.TRIGGERING_PLAYER)` | "
         "`Triggers.playerCastsSpell().anySpell()` | Aven Shrine (ODY) |"),
        ("| `CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect()` |",
         "| \"Whenever a player casts a spell, that player creates X 1/1 green Squirrel creature tokens, where X is the "
         "number of cards in all graveyards with the same name as that spell\" | `CreateTokenForTriggeringPlayerEffect(new "
         "CreateTokenEffect(" + SAME_NAME[1:-1] + ", \"Squirrel\", 1, 1, GREEN, List.of(SQUIRREL), Set.of(), Set.of()))` | "
         "`Triggers.playerCastsSpell().anySpell()` | Nantuko Shrine (ODY) |"),
        ("| `DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect()` |",
         "| \"Whenever a player casts a spell, this enchantment deals X damage to that player, where X is twice the number "
         "of cards in all graveyards with the same name as that spell\" | `DealDamageToPlayersEffect(new Scaled("
         + SAME_NAME[1:-1] + ", 2), DamageRecipient.TRIGGERING_PLAYER)` | `Triggers.playerCastsSpell().anySpell()` | "
         "Dwarven Shrine (ODY) |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| `m/MagmabloodArchaic.java` |",
         "| Converge-scaled instant/sorcery cast trigger — team power boost | `m/MagmabloodArchaic.java` | "
         "`Triggers.youCastSpell().withFilter(CardAnyOfPredicate(INSTANT, SORCERY))` + `BoostAllOwnCreaturesEffect(new "
         "ColorsSpentToCast(), new Fixed(0))`; the cast collector gives the ability the spell's colors spent as its X |"),
        ("| `a/AvenShrine.java` |",
         "| Spell cast trigger — caster gains for same-name graveyard cards | `a/AvenShrine.java` | "
         "`Triggers.playerCastsSpell().anySpell()` + `GainLifeEffect(" + SAME_NAME[1:-1] + ", TRIGGERING_PLAYER)` |"),
        ("| `n/NantukoShrine.java` |",
         "| Spell cast trigger — caster creates Squirrels for same-name graveyard cards | `n/NantukoShrine.java` | "
         "`Triggers.playerCastsSpell().anySpell()` + `CreateTokenForTriggeringPlayerEffect(CreateTokenEffect("
         + SAME_NAME[1:-1] + ", Squirrel 1/1))` |"),
        ("| `c/CabalShrine.java` |",
         "| Spell cast trigger — caster discards for same-name graveyard cards | `c/CabalShrine.java` | "
         "`Triggers.playerCastsSpell().anySpell()` + `DiscardEffect(" + SAME_NAME[1:-1] + ", TRIGGERING_PLAYER)` |"),
        ("| `i/InfernalKirin.java` |",
         "| Spirit/Arcane spell cast trigger — target player discards by mana value | `i/InfernalKirin.java` | "
         "`Triggers.youCastSpell().withFilter(CardAnyOf(SPIRIT, ARCANE))` + `RevealHandAndDiscardMatchingCardsEffect(new "
         "CardManaValueEqualsXPredicate())`; the spell's mana value is the ability's X |"),
        ("| `c/CelestialKirin.java` |",
         "| Spell cast trigger — destroy permanents by mana value | `c/CelestialKirin.java` | "
         "`Triggers.youCastSpell().withFilter(CardAnyOf(SPIRIT, ARCANE))` + `DestroyAllPermanentsEffect(new "
         "PermanentManaValueEqualsXPredicate())`; the spell's mana value is the ability's X |"),
        ("| `v/VeiledSentry.java` |",
         "| Spell cast trigger — self-animation by mana value | `v/VeiledSentry.java` | "
         "`Triggers.opponentCastsSpell().anySpell()` with `TriggerOptions.DEFAULT.withInterveningIf(new "
         "SourceIsEnchantment())` + `BecomeCreatureEffect(new TargetSpellManaValue(), new TargetSpellManaValue(), "
         "ILLUSION)`; the spell's mana value as the ability resolves, as it last existed if it was countered |"),
    ],
    "PREDICATES_REFERENCE.md": [
        ("| `CardSharesNameWithAPermanentPredicate` |",
         "KEEP\n| `CardSharesNameWithTriggeringSpellPredicate` | `()` | a card with the same name as the spell that "
         "triggered the resolving ability, read as it resolves (the live spell, else the entry's snapshot; a face-down "
         "spell has no name). Read by `CardsInGraveyard` (the Odyssey shrines: \"cards in all graveyards with the same "
         "name as that spell\"); matches nothing elsewhere |"),
    ],
    "ARCHITECTURE.md": [
        ("\"Damage equal to the amount of mana spent to cast that spell\" (`ManaSpentToCast`) reads the X the cast collector gives the ability, the mana spent on the triggering spell.",
         "APPEND: An effect's own filter \"with that spell's mana value\" (`DestroyAllPermanentsEffect` with "
         "`PermanentManaValueEqualsXPredicate`, `RevealHandAndDiscardMatchingCardsEffect` with "
         "`CardManaValueEqualsXPredicate`: the Kirins) reads the spell's mana value as the ability's X the same way, and "
         "\"for each color of mana spent to cast that spell\" (`ColorsSpentToCast`) the colors spent on it. \"That player\" "
         "of a cast trigger is the caster, the non-targeting entry's `targetId` the `TRIGGERING_PLAYER` recipients read, "
         "and \"the same name as that spell\" is `CardSharesNameWithTriggeringSpellPredicate`, which the "
         "`CardsInGraveyard` count reads from the entry's triggering spell."),
    ],
}


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        lines = path.read_text().split("\n")
        changed = False
        for marker, new in edits:
            hits = [i for i, line in enumerate(lines) if marker in line]
            if new is not None and new.startswith("APPEND: "):
                extra = new[len("APPEND: "):]
                if any(extra in line for line in lines):
                    continue
                if len(hits) != 1:
                    raise SystemExit(f"{name}: marker not found once: {marker}")
                lines[hits[0]] = lines[hits[0]] + " " + extra
                changed = True
                continue
            if new is not None and new.startswith("KEEP\n"):
                extra = new[len("KEEP\n"):]
                if extra in lines:
                    continue
                if len(hits) != 1:
                    raise SystemExit(f"{name}: marker not found once: {marker}")
                lines.insert(hits[0] + 1, extra)
                changed = True
                continue
            if not hits:
                continue
            if len(hits) != 1:
                raise SystemExit(f"{name}: marker found {len(hits)} times: {marker}")
            new_lines = None if new is None else new.split("\n")
            if new_lines is None:
                del lines[hits[0]]
            elif lines[hits[0]:hits[0] + len(new_lines)] != new_lines:
                lines[hits[0]:hits[0] + 1] = new_lines
            else:
                continue
            changed = True
        if changed:
            path.write_text("\n".join(lines))
            print(f"updated {name}")
    stale = []
    for path in list(DOCS.glob("*.md")) + list(SKILLS.rglob("*.md")):
        text = path.read_text()
        stale += [f"{path.name}: {cls}" for cls in DELETED if cls in text]
    if stale:
        raise SystemExit("still named:\n" + "\n".join(stale))


if __name__ == "__main__":
    main()
