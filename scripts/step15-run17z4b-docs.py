#!/usr/bin/env python3
"""Step 15, run 17z4b: the agent-docs lines that name the removed land-tap mana effects
(`AddProducedManaWhenSnowLandTappedEffect`, `AddProducedManaWhenLandOfSubtypeTappedEffect`,
`TappedSnowLandDoesntUntapEffect`, the `EFFECT_MERGE_PLAN.md` land-tap trio merged into
`AddOneOfEachManaTypeProducedByLandEffect`), the effect's lost `landFilter` component (the trigger's `withFilter`),
Regal Behemoth's `anyColor(controllerOnly, monarchOnly)`, and the produced-mana effects resolved by their own handlers
(`AddOneOfEachManaTypeProducedByLandEffect`, `AddManaForEachOtherLandWithSameNameEffect`).

Whole-line edits keyed by the line's start (the old line is replaced, or a new line inserted after an anchor line),
or a fragment replaced inside a line, so a second run changes nothing. Fails if a doc or skill still names a removed
effect outside a "Gone" line.

Usage: step15-run17z4b-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RUN = "effect-slot-refactor.md step 15, run 17z4b"
QR = "agent-docs/EFFECTS_QUICK_REFERENCE.md"
IX = "agent-docs/EFFECTS_INDEX.md"
OM = "agent-docs/ORACLE_TEXT_EFFECT_MAP.md"
PS = "agent-docs/CARD_PATTERNS_PERMANENTS_STATIC.md"
CP = "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md"
MP = "agent-docs/EFFECT_MERGE_PLAN.md"

ONE = "`AddOneOfEachManaTypeProducedByLandEffect(false)`"
WINTER = ("`Triggers.playerTapsLandForMana().withFilter(new PermanentHasSupertypePredicate(CardSupertype.SNOW))` + "
          "`SequenceEffect.of(new AddOneOfEachManaTypeProducedByLandEffect(false), "
          "new SkipNextUntapEffect(TapUntapScope.TRIGGERING))`")
KEEPER = ("`Triggers.playerTapsLandForMana().withFilter(new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MOUNTAIN, "
          "CardSubtype.FOREST, CardSubtype.PLAINS)))` + " + ONE)
HANDLED = ("a triggered mana ability (CR 605.1b) the land-tap collector resolves at once (CR 605.4a) through the "
           "effect's handler, which reads the stack entry's produced mana colors (the land's basic-land-type colors "
           "as it last existed when the tap reported none) and adds to the entry's triggering player; while the "
           "land's own color choice is open the entry waits with this effect for the produced color")

EDITS = [
    (OM, "| \"whenever a player taps a [type], [type], or [type] for mana, that player adds one mana of any type that land produced\"",
     "| \"whenever a player taps a [type], [type], or [type] for mana, that player adds one mana of any type that land "
     f"produced\" | {KEEPER} | Triggers.playerTapsLandForMana() | Keeper of Progenitus. Symmetric; the land types are the "
     "trigger's filter, judged on the tapped land as it is tapped (once per land even with several matching types); "
     "a land that produced several types lets its player choose one |", "replace"),
    (OM, "| \"whenever a player taps a snow land for mana, that player adds one mana of any type that land produced\"",
     "| \"whenever a player taps a snow land for mana, that player adds one mana of any type that land produced. That "
     f"land doesn't untap during its controller's next untap step\" | {WINTER} | Triggers.playerTapsLandForMana() | "
     "Winter's Night: one triggered mana ability, both sentences resolving at once; the opponent-only, any-land "
     "untap lock that uses the stack is `SkipNextUntapEffect(TapUntapScope.TRIGGERING)` on "
     "`Triggers.opponentTapsLandForMana()` (Vorinclex, Voice of Hunger) |", "replace"),
    (OM, "| \"that land doesn't untap during its controller's next untap step\" (snow land tap)",
     "| \"whenever a player taps a basic land for mana, that player adds one mana of any type that land produced\" | "
     f"`Triggers.playerTapsLandForMana().withFilter(new PermanentHasSupertypePredicate(CardSupertype.BASIC))` + {ONE} | "
     "Triggers.playerTapsLandForMana() | Lavaleaper. The land filter is the trigger's `withFilter` |", "replace"),
    (OM, "| \"Whenever you tap a land for mana while you're the monarch, add an additional one mana of any type that land produced\"",
     "| \"Whenever you tap a land for mana while you're the monarch, add an additional one mana of any color\" | "
     "`addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeMonarchEffect())` + "
     "`AddOneOfEachManaTypeProducedByLandEffect.anyColor(true, true)` | Triggers.enters(ObjectRelation.THIS) + "
     "Triggers.playerTapsLandForMana() | Regal Behemoth (CMM 316). \"Any color\" is a choice of the five colors "
     "(CR 106.1a), never colorless and independent of the land's mana, so it never waits for the land's color choice |",
     "replace"),
    (OM, "| \"whenever a land you control is tapped for mana, add an additional one mana of any type that land produced for each other land you control with the same name as it\"",
     "| \"whenever a land you control is tapped for mana, add an additional one mana of any type that land produced for "
     "each other land you control with the same name as it\" | `AddManaForEachOtherLandWithSameNameEffect()` | "
     "Triggers.playerTapsLandForMana() | Sasaya's Essence: the lands are counted as the triggered mana ability "
     "resolves, by the tapped land's name as it is or as it last existed (a land sacrificed for its mana still "
     "counts its name), each mana of the type the land produced (any combination when it produced several) |",
     "replace"),
    (IX, "| `AddManaForEachOtherLandWithSameNameEffect` |",
     "| `AddManaForEachOtherLandWithSameNameEffect` | `()` | Triggers.playerTapsLandForMana() trigger, the source's "
     "controller tapping: one mana of a type the land produced for every other land with the same name the tapping "
     f"player controls, counted as it resolves (Sasaya's Essence); {HANDLED} |", "replace"),
    (IX, "| `AddOneOfEachManaTypeProducedByLandEffect` | `(boolean controllerOnly)`",
     "| `AddOneOfEachManaTypeProducedByLandEffect` | `(boolean controllerOnly)`, `(boolean controllerOnly, boolean "
     "matchesImprintedCardName)` or `anyColor(boolean controllerOnly, boolean monarchOnly)` | "
     "Triggers.playerTapsLandForMana() trigger: when a land taps for mana, the tapping player adds one additional "
     "mana of a type that land produced, choosing one when it produced several. `controllerOnly=true` fires only for "
     "the source controller's land taps (Vorinclex, Voice of Hunger); `controllerOnly=false` is symmetric (Mana "
     "Flare). Which lands count is the trigger's `withFilter` (Lavaleaper's basic, Winter's Night's snow, Keeper of "
     "Progenitus's Mountain, Forest or Plains). `anyColor` is Regal Behemoth's \"one mana of any color\" (the five "
     f"colors) with `monarchOnly`; {HANDLED}. A step of a `SequenceEffect` (Overabundance, Barbflare Gremlin, Winter's "
     "Night): the steps after it wait for its choice |", "replace"),
    (IX, "| `AddProducedManaWhenLandOfSubtypeTappedEffect` |",
     "| `AddProducedManaWhenLandOfSubtypeTappedEffect` | — | **Gone** (" + RUN + "): Keeper of Progenitus writes "
     f"{KEEPER} |", "replace"),
    (IX, "| `AddProducedManaWhenSnowLandTappedEffect` |",
     "| `AddProducedManaWhenSnowLandTappedEffect` | — | **Gone** (" + RUN + "): Winter's Night writes "
     f"{WINTER} |", "replace"),
    (IX, "| `TappedSnowLandDoesntUntapEffect` |",
     "| `TappedSnowLandDoesntUntapEffect` | — | **Gone** (" + RUN + "): Winter's Night's untap lock is the second "
     f"step of its one triggered mana ability, {WINTER} |", "replace"),
    (QR, "- `AddManaForEachOtherLandWithSameNameEffect()` —",
     "- `AddManaForEachOtherLandWithSameNameEffect()` — Triggers.playerTapsLandForMana(); when the controller taps a "
     "land for mana, adds one mana of a type that land produced for every other land with the same name they control, "
     "counted as the triggered mana ability resolves (Sasaya's Essence)", "replace"),
    (QR, "- `AddProducedManaWhenSnowLandTappedEffect()` —",
     "- `AddProducedManaWhenSnowLandTappedEffect()` — **Gone** (" + RUN + "): Winter's Night writes " + WINTER,
     "replace"),
    (QR, "- `TappedSnowLandDoesntUntapEffect()` —",
     "- `TappedSnowLandDoesntUntapEffect()` — **Gone** (" + RUN + "): the second step of Winter's Night's "
     "triggered mana ability, `SkipNextUntapEffect(TapUntapScope.TRIGGERING)`", "replace"),
    (PS, "| Monarch-gated any-color land-tap mana |",
     "| Monarch-gated any-color land-tap mana | `r/RegalBehemoth.java` | Triggers.playerTapsLandForMana() "
     "`AddOneOfEachManaTypeProducedByLandEffect.anyColor(true, true)` — the extra mana is any color (a five-color "
     "choice, never colorless), not a type the land produced; `monarchOnly` gates it to the monarch |", "replace"),
    (PS, "| Snow-land tap mana boost + symmetric untap lock |",
     f"| Snow-land tap mana boost + symmetric untap lock | `w/WintersNight.java` | {WINTER} — World enchantment; one "
     "triggered mana ability: every snow land tapped for mana pays its controller an extra mana of the produced type "
     "and then skips its next untap step |", "replace"),
    (CP, "| Land tap triggered mana ability with damage |",
     "| Land tap triggered mana ability with damage | `o/Overabundance.java` | Triggers.playerTapsLandForMana() "
     "`SequenceEffect.of(AddOneOfEachManaTypeProducedByLandEffect(false), DealDamageToPlayersEffect(1, "
     "DamageRecipient.TRIGGERING_PLAYER))` — one triggered mana ability, each step resolved at once by its handler "
     "without using the stack; the damage waits for the extra mana's type choice when the land produced several |",
     "replace"),
    (MP, "| `AddOneOfEachManaTypeProducedByLandEffect(boolean controllerOnly, PermanentPredicate landFilter)` |",
     "| `AddOneOfEachManaTypeProducedByLandEffect(boolean controllerOnly)` + the trigger's `withFilter` | "
     "`AddProducedManaWhenLandOfSubtypeTappedEffect`, `AddProducedManaWhenSnowLandTappedEffect` (and "
     "`TappedSnowLandDoesntUntapEffect`): **Gone** (" + RUN + ") | 1 + 1 |", "replace"),
]

BLOCKS = [
    (MP, """- Land-tap trio: the three collectors in `trigger/LandTapTriggerCollectorService.java:205,233,263` are
  token-for-token identical including a byte-identical log line; only the land-qualification guard
  differs. The snow guard uses layer-aware `hasEffectiveSupertype`, so predicate evaluation must use
  the layer-aware path.""",
     """- Land-tap trio: done in """ + RUN + """. The land qualification is the trigger's `withFilter`
  (judged by `TriggerCollectionService.landTapMatches` through the layer-aware predicate path), and the
  survivor resolves by its own handler, so it lost its `landFilter` component as well."""),
]

GONE = ["AddProducedManaWhenSnowLandTappedEffect", "AddProducedManaWhenLandOfSubtypeTappedEffect",
        "TappedSnowLandDoesntUntapEffect"]


def apply(lines, start, new, mode):
    idx = [i for i, line in enumerate(lines) if line.startswith(start)]
    if mode == "after" or mode.startswith("after-anchor:"):
        if new in lines:
            return False
        anchor = mode[len("after-anchor:"):] if mode.startswith("after-anchor:") else start
        at = [i for i, line in enumerate(lines) if line.startswith(anchor)]
        if not at:
            raise SystemExit(f"anchor not found: {anchor}")
        lines.insert(at[0] + 1, new)
        return True
    if mode.startswith("fragment:"):
        old, repl = mode[len("fragment:"):].split("=>", 1)
        for i in idx:
            if repl in lines[i]:
                return False
            if old in lines[i]:
                lines[i] = lines[i].replace(old, repl, 1)
                return True
        raise SystemExit(f"fragment not found under {start}: {old}")
    if new in lines:
        return False
    if not idx:
        raise SystemExit(f"line not found: {start}")
    lines[idx[0]] = new
    return True


def main():
    for path, old, new in BLOCKS:
        file = ROOT / path
        text = file.read_text()
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"block not found in {path}: {old[:60]}")
        file.write_text(text.replace(old, new))
        print(f"updated {path}")
    by_file = {}
    for path, start, new, mode in EDITS:
        by_file.setdefault(path, []).append((start, new, mode))
    for path, edits in by_file.items():
        file = ROOT / path
        lines = file.read_text().split("\n")
        changed = False
        for start, new, mode in edits:
            changed |= apply(lines, start, new, mode)
        if changed:
            file.write_text("\n".join(lines))
            print(f"updated {path}")
    docs = list((ROOT / "agent-docs").glob("*.md")) + list((ROOT / ".claude/skills").rglob("SKILL.md")) \
        + list((ROOT / ".codex/skills").rglob("SKILL.md"))
    for doc in docs:
        for number, line in enumerate(doc.read_text().split("\n"), 1):
            for gone in GONE:
                if re.search(r"\b" + gone + r"\b", line) and "**Gone**" not in line:
                    raise SystemExit(f"{doc.relative_to(ROOT)}:{number} still names {gone}")


if __name__ == "__main__":
    main()
