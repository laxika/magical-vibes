#!/usr/bin/env python3
"""Step 15, run 17x: the agent docs for the storm family's copy counts.

Drops or rewrites every doc line naming a removed record (`StormEffect`, `StormCopyEffect`, `ReplicateEffect`,
`CopyThisSpellForXValueEffect`, `GravestormEffect`, `CoststormEffect`, `CopyThisSpellForEachCounterKindEffect`,
`CopyThisSpellForEachCommanderCastEffect`, `OtherInstantsAndSorceriesCastCountEffect`, `SpellCastCopyTriggerEffect`),
documents the new amounts (`SpellsCastBeforeTriggeringSpellThisTurn.byAnyPlayer()`,
`PermanentsPutIntoGraveyardFromBattlefieldThisTurn`, `DistinctManaValuesAmongOtherSpellsAndLandsPlayedThisTurn`) and
`CopyTriggeringSpellEffect.storm()`, and adds a sentence to `ARCHITECTURE.md`'s triggering-spell paragraph.

Lines are matched by their start (a doc line holding one effect or one oracle-text row), so the script is idempotent:
a line already rewritten no longer starts with the old text. It fails if a doc or skill still names a removed form.

Usage: step15-run17x-docs.py
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

STORM = ("- `CopyTriggeringSpellEffect.storm()` — Storm keyword (CR 702.40a). Place in the `Triggers.youCastThisSpell()` "
         "ability: `CopyTriggeringSpellEffect().withCopies(SpellsCastBeforeTriggeringSpellThisTurn.byAnyPlayer())`, one "
         "copy for each spell any player cast before it this turn, counted as the ability resolves up to the spell's "
         "place in the turn's cast order (kept on the stack entry, so a spell cast in response doesn't count). Each copy "
         "may choose new targets (Dragonstorm). Add `permanentSpellCopiesAreTokens()` when the copies of a permanent "
         "spell become tokens (Stormscale Scion), and `withoutSupertypes(Set.of(CardSupertype.LEGENDARY))` when they "
         "also aren't legendary (Aeve, Progenitor Ooze). A permanent granting storm puts the same effect in "
         "`GrantCastTriggerToSpellsEffect(SpellCastTriggerGrant.STORM_TO_INSTANT_OR_SORCERY, CopyTriggeringSpellEffect.storm())` "
         "(Prismari, the Inspiration); an emblem whose spells have storm is a cast trigger with it (Ral, Crackling Wit)")
REPLICATE = ("- Replicate (CR 702.56a): `addCastingCost(new RepeatableAdditionalManaCost(List.of(\"[cost]\")))` + "
             "`addTrigger(Triggers.youCastThisSpell(), TriggerOptions.DEFAULT.withInterveningIf(new "
             "RepeatedAdditionalCostPaid(\"[cost]\")), new CopyTriggeringSpellEffect().withCopies(new "
             "RepeatedAdditionalCostCount(\"[cost]\")))`: one copy for each payment, the triggering spell's payments "
             "kept on the stack entry; it doesn't trigger when none was paid (Gigadrowse). Add "
             "`permanentSpellCopiesAreTokens()` for a permanent spell whose copies become tokens (Changing Loyalty). A tap "
             "replicate whose count is the spell's X: `withInterveningIf(new SpellXAtLeast(1))` and `withCopies(new "
             "TriggeringSpellXValue())` (Exterminate!)")
GRAVESTORM = ("- Gravestorm (CR 702.69a): `addTrigger(Triggers.youCastThisSpell(), new CopyTriggeringSpellEffect()"
              ".withCopies(new PermanentsPutIntoGraveyardFromBattlefieldThisTurn()))`, counted as the ability resolves: "
              "one copy for each permanent put into a graveyard from the battlefield this turn, tokens included (Bitter "
              "Ordeal)")
COSTSTORM = ("- Coststorm: `addTrigger(Triggers.youCastThisSpell(), new CopyTriggeringSpellEffect().withCopies(new "
             "DistinctManaValuesAmongOtherSpellsAndLandsPlayedThisTurn()))`, counted as the ability resolves: one copy "
             "for each different mana value among the controller's other spells (their X included) and lands played "
             "this turn (Friarball, MB2 278/514)")
COMMANDER = ("- \"Copy it for each time you've cast your commander from the command zone this game\": "
             "`new CopyTriggeringSpellEffect().withCopies(new CommanderCastsFromCommandZoneThisGame())`, counted as the "
             "ability resolves (Echo Storm, MOC 221); `permanentSpellCopiesAreTokens()` for a permanent spell (Hatut "
             "Zeraze Strike Force, MSC 15)")
COUNTER_KINDS = ("- \"Copy it for each kind of counter among permanents you control\": `new CopyTriggeringSpellEffect()"
                 ".withCopies(new DistinctCounterKindsAmongControlledPermanents())`, counted as the ability resolves "
                 "(Storm of Forms, NCC 32)")

INDEX_STORM = ("| `CopyTriggeringSpellEffect.storm()` | `()` | Storm keyword (CR 702.40a) in a `Triggers.youCastThisSpell()` "
               "ability: `CopyTriggeringSpellEffect().withCopies(SpellsCastBeforeTriggeringSpellThisTurn.byAnyPlayer())`, "
               "one copy for each spell any player cast before the triggering spell this turn, counted as it resolves up "
               "to the spell's place in the turn's cast order. `permanentSpellCopiesAreTokens()` / "
               "`withoutSupertypes(Set.of(LEGENDARY))` for token copies. Replicate, gravestorm, coststorm, \"copy it X "
               "times\", the commander-cast and counter-kind copies are the same effect with `withCopies` of "
               "`RepeatedAdditionalCostCount` (behind the intervening \"if\" `RepeatedAdditionalCostPaid`), "
               "`PermanentsPutIntoGraveyardFromBattlefieldThisTurn`, `DistinctManaValuesAmongOtherSpellsAndLandsPlayedThisTurn`, "
               "`TriggeringSpellXValue`, `CommanderCastsFromCommandZoneThisGame`, `DistinctCounterKindsAmongControlledPermanents` "
               "| Dragonstorm, Stormscale Scion, Aeve, Progenitor Ooze, Gigadrowse, Bitter Ordeal, Friarball, Echo Storm, "
               "Storm of Forms |")

# doc -> [(line start, replacement or None to drop)]
EDITS = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `GravestormEffect()`", GRAVESTORM),
        ("- `StormCopyEffect(..., SpellCastCopyTriggerEffect resolutionCount)`", None),
        ("- `CoststormEffect()`", COSTSTORM),
        ("- `CopyThisSpellForEachCommanderCastEffect()`", COMMANDER),
        ("- `ReplicateEffect(String manaCost[, boolean tokenCopy])`", REPLICATE),
        ("- `StormEffect()` — Storm keyword. Place in the `Triggers.youCastThisSpell()` ability; at cast time queues a "
         "`StormCopyEffect` that copies the spell once per spell cast before it this turn (all players). Each copy may "
         "choose new targets (Dragonstorm). Use", STORM),
        ("- `ReplicateEffect(String manaCost)`", None),
        ("- `StormEffect()` — Storm keyword.", None),
        ("- `CopyThisSpellForEachCounterKindEffect()`", COUNTER_KINDS),
        ("- `SpellsCastBeforeTriggeringSpellThisTurn(CardPredicate filter)` — the matching spells the triggering spell's "
         "caster cast",
         "- `SpellsCastBeforeTriggeringSpellThisTurn(CardPredicate filter[, boolean anyPlayer])` — the matching spells "
         "cast this turn before the triggering spell, its caster's, or with `anyPlayer` (`byAnyPlayer()`, the storm "
         "count) every player's, counted as the ability resolves up to the triggering spell's place among them (kept on "
         "the stack entry as the ability triggers, so a spell cast in response doesn't count; a countered spell was "
         "still cast and counts). \"Copy it for each other instant and sorcery spell you've cast before it this turn\" "
         "(Thousand-Year Storm): `new CopyTriggeringSpellEffect().withCopies(new "
         "SpellsCastBeforeTriggeringSpellThisTurn(instantOrSorcery))`; storm: `CopyTriggeringSpellEffect.storm()`\n"
         "- `PermanentsPutIntoGraveyardFromBattlefieldThisTurn()` - the permanents put into graveyards from the "
         "battlefield this turn, every player's, tokens included (gravestorm)\n"
         "- `DistinctManaValuesAmongOtherSpellsAndLandsPlayedThisTurn()` - the different mana values among the "
         "controller's other spells cast (X included) and lands played this turn; \"other\" is the stack entry's "
         "triggering spell (coststorm)"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `CoststormEffect` |", None),
        ("| `GravestormEffect` |", None),
        ("| `CopyThisSpellForEachCommanderCastEffect` |", None),
        ("| `StormEffect` |", INDEX_STORM),
        ("| `ReplicateEffect` |", None),
        ("| `OtherInstantsAndSorceriesCastCountEffect` |", None),
        ("| `StormCopyEffect` |", None),
        ("| `CopyThisSpellForEachCounterKindEffect` |", None),
        ("- `SpellsCastBeforeTriggeringSpellThisTurn(CardPredicate filter)` — the matching spells the triggering spell's "
         "caster cast",
         "- `SpellsCastBeforeTriggeringSpellThisTurn(CardPredicate filter[, boolean anyPlayer])` — the matching spells "
         "cast this turn before the triggering spell, its caster's, or with `anyPlayer` (`byAnyPlayer()`, the storm "
         "count, CR 702.40a) every player's, counted as the ability resolves up to the triggering spell's place among "
         "them (`StackEntry.triggeringSpellCastIndex` / `triggeringSpellTurnCastIndex`, kept as the ability triggers, so "
         "a spell cast in response doesn't count; a countered spell was still cast and counts). Thousand-Year Storm: "
         "`new CopyTriggeringSpellEffect().withCopies(new SpellsCastBeforeTriggeringSpellThisTurn(instantOrSorcery))`; "
         "storm: `CopyTriggeringSpellEffect.storm()`\n"
         "- `PermanentsPutIntoGraveyardFromBattlefieldThisTurn()` — the permanents put into graveyards from the "
         "battlefield this turn, every player's, tokens included (gravestorm, CR 702.69a)\n"
         "- `DistinctManaValuesAmongOtherSpellsAndLandsPlayedThisTurn()` — the different mana values among the "
         "controller's other spells cast (their X included) and lands played this turn, \"other\" the stack entry's "
         "triggering spell (coststorm: Friarball)"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Create a 2/2 white Monk creature token. Coststorm",
         "| \"Create a 2/2 white Monk creature token. Coststorm (When you cast this spell, copy it for each different mana "
         "value among other spells and lands you've played this turn.)\" | `SPELL CreateTokenEffect(\"Monk\", 2, 2, WHITE, "
         "[MONK])` + `Triggers.youCastThisSpell() CopyTriggeringSpellEffect().withCopies(new "
         "DistinctManaValuesAmongOtherSpellsAndLandsPlayedThisTurn())` | SPELL + Triggers.youCastThisSpell() | Friarball "
         "(MB2 278/514). Counted as the ability resolves |"),
        ("| \"Replicate {1}{B}. Creature cards in your graveyard perpetually get +1/+1.",
         "| \"Replicate {1}{B}. Creature cards in your graveyard perpetually get +1/+1. Then return target creature card "
         "with mana value 3 or less from your graveyard to the battlefield.\" | `SPELL RepeatableAdditionalManaCost("
         "List.of(\"{1}{B}\"))` + `SPELL PerpetuallyBoostCreatureCardsInGraveyardEffect(1, 1)` + `SPELL "
         "ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(CardAllOfPredicate(CREATURE, "
         "CardMaxManaValuePredicate(3))).targetGraveyard(true).build()` + `Triggers.youCastThisSpell()` with "
         "`TriggerOptions.DEFAULT.withInterveningIf(new RepeatedAdditionalCostPaid(\"{1}{B}\"))` and "
         "`CopyTriggeringSpellEffect().withCopies(new RepeatedAdditionalCostCount(\"{1}{B}\"))` | SPELL + "
         "Triggers.youCastThisSpell() | Feed the Bog (YSOS 6). Each copy repeats both the perpetual boost and the "
         "targeted return. |"),
        ("| \"Search target player's library for a card and exile it. Then that player shuffles. Gravestorm",
         "| \"Search target player's library for a card and exile it. Then that player shuffles. Gravestorm (When you "
         "cast this spell, copy it for each permanent put into a graveyard this turn. You may choose new targets for the "
         "copies.)\" | `SearchTargetLibraryEffect(1, null, LibrarySearchDestination.EXILE, false)` + "
         "`CopyTriggeringSpellEffect().withCopies(new PermanentsPutIntoGraveyardFromBattlefieldThisTurn())` | SPELL + "
         "Triggers.youCastThisSpell() | Bitter Ordeal (FUT). Targets any player; the mandatory whole-library search "
         "exiles the chosen card face down and then shuffles that player's library. Gravestorm counts, as it resolves, "
         "the permanents put into graveyards from the battlefield during the turn, including tokens |"),
        ("| \"When you cast this spell, copy it for each time you've cast your commander from the command zone this game. "
         "When this creature enters",
         "| \"When you cast this spell, copy it for each time you've cast your commander from the command zone this game. "
         "When this creature enters, destroy up to one target artifact or enchantment.\" | `CopyTriggeringSpellEffect()"
         ".permanentSpellCopiesAreTokens().withCopies(new CommanderCastsFromCommandZoneThisGame())` + "
         "`target(artifactOrEnchantment, 0, 1).addTrigger(Triggers.enters(ObjectRelation.THIS), "
         "DestroyTargetPermanentEffect())` | Triggers.youCastThisSpell() + Triggers.enters(ObjectRelation.THIS) | Hatut "
         "Zeraze Strike Force (MSC 15). The count is read as the ability resolves; copies of this permanent spell enter "
         "as tokens |"),
        ("| \"When you cast this spell, copy it for each time you've cast your commander from the command zone this game. "
         "You may choose new targets",
         "| \"When you cast this spell, copy it for each time you've cast your commander from the command zone this game. "
         "You may choose new targets for the copies. Create a token that's a copy of target artifact.\" | "
         "`CopyTriggeringSpellEffect().withCopies(new CommanderCastsFromCommandZoneThisGame())` + "
         "`target(TargetFilters.artifact()).addSpellEffect(CreateTokenCopyOfTargetPermanentEffect())` | "
         "Triggers.youCastThisSpell() + SPELL | Echo Storm (MOC 221). Counted as the ability resolves; ordinary copies "
         "retain the spell's target and offer the copy controller the new-target choice |"),
        ("| \"Replicate [cost]. Tap target permanent.\"",
         "| \"Replicate [cost]. Tap target permanent.\" | `addCastingCost(new RepeatableAdditionalManaCost(List.of("
         "\"[cost]\")))` + `target(TargetFilters.permanent()).addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET))` "
         "+ `addTrigger(Triggers.youCastThisSpell(), TriggerOptions.DEFAULT.withInterveningIf(new "
         "RepeatedAdditionalCostPaid(\"[cost]\")), new CopyTriggeringSpellEffect().withCopies(new "
         "RepeatedAdditionalCostCount(\"[cost]\")))` | SPELL + Triggers.youCastThisSpell() | Replicate (CR 702.56a) copies "
         "the spell once for each payment and doesn't trigger without one; each copy retains the original target and "
         "may choose new targets (Gigadrowse). For permanent spells whose copies become tokens, add "
         "`permanentSpellCopiesAreTokens()` (Changing Loyalty). |"),
        ("| \"Storm (When you cast this spell, copy it for each spell cast before it this turn.)\" | `StormEffect()` | "
         "Triggers.youCastThisSpell() | Dragonstorm. `TriggerCollectionService.checkSpellCastTriggers` snapshots the "
         "just-cast spell and queues a `StormCopyEffect(snapshot, caster, copies)` where `copies = "
         "gameData.getTotalSpellsCastThisTurnCount() - 1` (all players, minus this spell which is already recorded). "
         "Each copy may choose new targets. Use `StormEffect(true)` for permanent spells whose copies become tokens "
         "(Stormscale Scion), or",
         "| \"Storm (When you cast this spell, copy it for each spell cast before it this turn.)\" | "
         "`CopyTriggeringSpellEffect.storm()` | Triggers.youCastThisSpell() | Dragonstorm (CR 702.40a). Counted as the "
         "ability resolves: every player's spells before this one in the turn's cast order, its place kept on the stack "
         "entry. Each copy may choose new targets. Add `permanentSpellCopiesAreTokens()` for permanent spells whose "
         "copies become tokens (Stormscale Scion), and `withoutSupertypes(Set.of(CardSupertype.LEGENDARY))` when those "
         "token copies also lose the legendary supertype (Aeve, Progenitor Ooze) |"),
        ("| \"Storm (When you cast this spell, copy it for each spell cast before it this turn.)\" | `StormEffect()` |",
         None),
        ("| \"When you cast this spell, copy it for each kind of counter among permanents you control.",
         "| \"When you cast this spell, copy it for each kind of counter among permanents you control. You may choose new "
         "targets for the copies.\" | `Triggers.youCastThisSpell() CopyTriggeringSpellEffect().withCopies(new "
         "DistinctCounterKindsAmongControlledPermanents())` + `target(TargetFilters.nonlandPermanent())"
         ".addSpellEffect(ReturnToHandEffect.target())` | Triggers.youCastThisSpell() + SPELL | Storm of Forms (NCC 32); "
         "the distinct concrete counter kinds are counted as the ability resolves, and only the caster's permanents "
         "count. |"),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("| sorcery that creates a token and copies itself for each distinct prior spell or land mana value |",
         "| sorcery that creates a token and copies itself for each distinct prior spell or land mana value | "
         "`f/Friarball.java` + `CreateTokenEffect` + `Triggers.youCastThisSpell() CopyTriggeringSpellEffect().withCopies("
         "DistinctManaValuesAmongOtherSpellsAndLandsPlayedThisTurn)` |"),
        ("| cast-time copy for each distinct counter kind among controlled permanents |",
         "| cast-time copy for each distinct counter kind among controlled permanents | `s/StormOfForms.java` + "
         "`CopyTriggeringSpellEffect().withCopies(DistinctCounterKindsAmongControlledPermanents)` |"),
        ("| targeted bounce plus every other nonland permanent with the same mana value |",
         "| targeted bounce plus every other nonland permanent with the same mana value | `m/MistsOfLRien.java` + "
         "`ReturnTargetPermanentAndAllWithSameManaValueToHandEffect` + replicate (`RepeatedAdditionalCostPaid` / "
         "`RepeatedAdditionalCostCount`) |"),
        ("| replicate spell that perpetually boosts creature cards in your graveyard,",
         "| replicate spell that perpetually boosts creature cards in your graveyard, then returns a low-mana-value "
         "creature card | `f/FeedTheBog.java` + `PerpetuallyBoostCreatureCardsInGraveyardEffect` + "
         "`ReturnCardFromGraveyardEffect` + replicate (`RepeatedAdditionalCostPaid` / `RepeatedAdditionalCostCount`) |"),
    ],
}

# doc -> [(old, new)]: phrases inside longer lines.
PHRASES = {
    "EFFECTS_INDEX.md": [
        ("consumes it on the next matching cast and queues a `StormCopyEffect` for each pending grant",
         "consumes it on the next matching cast and queues a `CopyTriggeringSpellEffect.storm()` trigger for each "
         "pending grant"),
        ("consumes all pending grants on that next matching cast and queues one `StormCopyEffect` per grant, counting "
         "every spell cast earlier that turn",
         "consumes all pending grants on that next matching cast and queues one `CopyTriggeringSpellEffect.storm()` "
         "trigger per grant, counting as it resolves every spell cast before that spell this turn"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("+ `Triggers.youCastThisSpell() StormEffect` |", "+ `Triggers.youCastThisSpell() CopyTriggeringSpellEffect.storm()` |"),
        ("+ `Triggers.youCastThisSpell() ReplicateEffect(\"{U}\")` |",
         "+ `Triggers.youCastThisSpell()` replicate (`RepeatedAdditionalCostPaid(\"{U}\")` / "
         "`RepeatedAdditionalCostCount(\"{U}\")`) |"),
    ],
}

ARCHITECTURE_ANCHOR = ("which the cast collector keeps on the entry (`StackEntry.triggeringSpellCastIndex`).")
ARCHITECTURE_SENTENCE = (" Storm (`CopyTriggeringSpellEffect.storm()`, CR 702.40a) counts every player's spells before "
                         "the triggering spell the same way, up to its place in the turn's cast order "
                         "(`StackEntry.triggeringSpellTurnCastIndex`); the self-cast collector records the triggering "
                         "spell, both places and its X and repeatable additional cost payments on every \"copy this "
                         "spell\" entry, so replicate's, gravestorm's and the other counted copies' amounts read them "
                         "as the ability resolves.")

REMOVED = re.compile(r"\b(StormEffect|StormCopyEffect|ReplicateEffect|CopyThisSpellForXValueEffect|GravestormEffect"
                     r"|CoststormEffect|CopyThisSpellForEachCounterKindEffect|CopyThisSpellForEachCommanderCastEffect"
                     r"|OtherInstantsAndSorceriesCastCountEffect|SpellCastCopyTriggerEffect|SpellCastCopyTrigger)\b")


def edit(doc, edits):
    path = DOCS / doc
    lines = path.read_text().split("\n")
    out = []
    for line in lines:
        for start, replacement in edits:
            if line.startswith(start):
                if replacement is not None and replacement not in out:
                    out.extend(replacement.split("\n"))
                break
        else:
            out.append(line)
    text = "\n".join(out)
    if text != "\n".join(lines):
        path.write_text(text)
        print(f"edited {doc}")


def main():
    for doc, edits in EDITS.items():
        edit(doc, edits)
    for doc, phrases in PHRASES.items():
        path = DOCS / doc
        text = path.read_text()
        changed = text
        for old, new in phrases:
            changed = changed.replace(old, new)
        if changed != text:
            path.write_text(changed)
            print(f"edited {doc}")
    arch = DOCS / "ARCHITECTURE.md"
    text = arch.read_text()
    if ARCHITECTURE_SENTENCE not in text:
        if ARCHITECTURE_ANCHOR not in text:
            raise SystemExit("ARCHITECTURE.md: anchor not found")
        text = text.replace(ARCHITECTURE_ANCHOR, ARCHITECTURE_ANCHOR + ARCHITECTURE_SENTENCE, 1)
        arch.write_text(text)
        print("edited ARCHITECTURE.md")
    failures = []
    for path in [*DOCS.glob("*.md"), *(ROOT / ".claude/skills").rglob("*.md")]:
        for number, line in enumerate(path.read_text().split("\n"), 1):
            if REMOVED.search(line):
                failures.append(f"{path.relative_to(ROOT)}:{number}: {line[:160]}")
    if failures:
        raise SystemExit("still naming a removed form:\n" + "\n".join(failures))


if __name__ == "__main__":
    sys.exit(main())
