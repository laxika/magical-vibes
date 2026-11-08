#!/usr/bin/env python3
"""Step 15, run 17zb2: the agent-docs lines that name the removed cast markers of Loki, God of Lies, Spellweaver
Volute and Shiko and Narset, Unified, the changed forms of `CreateTokenCopyOfTargetedSpellPermanentEffect` and
`CopyEnchantedInstantAndMayCastCopyEffect`, the new conditions `TriggeringSpellMatches` and `SpellCopiedThisWay`, and
`PermanentIsTargetOfTriggeringSpellPredicate`'s targets as the ability triggered.

Whole-line edits keyed by the line's start (the old line is replaced, or a new line inserted after an anchor line),
or a fragment replaced inside a line, so a second run changes nothing. Fails if a doc or skill still names a removed
marker outside a "Gone" line.

Usage: step15-run17zb2-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RUN = "effect-slot-refactor.md step 15, run 17zb2"
QR = "agent-docs/EFFECTS_QUICK_REFERENCE.md"
IX = "agent-docs/EFFECTS_INDEX.md"
OM = "agent-docs/ORACLE_TEXT_EFFECT_MAP.md"
PR = "agent-docs/PREDICATES_REFERENCE.md"

LOKI = ("`Triggers.youCastSpell().withStackEntryFilter(new StackEntryTargetsOnlySingleCreaturePredicate())` + "
        "`GainControlOfAllPermanentsMatchingEffect(new PermanentIsTargetOfTriggeringSpellPredicate(), "
        "ControlDuration.END_OF_TURN, new ConditionalEffect(new ControllerTurn(), SequenceEffect.of("
        "new UntapPermanentsEffect(TapUntapScope.TARGET), new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))))`")
VOLUTE = ("`SpellweaverVoluteEnterEffect()` + `Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.SORCERY))` "
          "with `new MayEffect(new CopyEnchantedInstantAndMayCastCopyEffect(), \"…\")`")
FLURRY = ("`Triggers.youCastSpell().nthThisTurn(2)` + `new ConditionalEffect(new TriggeringSpellMatches(new "
          "StackEntryAnyOfPredicate(List.of(new StackEntryTargetsPermanentPredicate(new PermanentTruePredicate()), new "
          "StackEntryTargetsAnyPlayerPredicate()))), new CopyTriggeringSpellEffect())` + `new ConditionalEffect(new "
          "NotCondition(new SpellCopiedThisWay()), new DrawCardEffect())`")
VESUVAN = ("`Triggers.youCastSpell().withStackEntryFilter(StackEntryPredicate)` + "
           "`CreateTokenCopyOfTargetedSpellPermanentEffect(CreateTokenCopyOfTargetPermanentEffect.nonLegendary(...))`")
TOKEN_COPY = ("- `CreateTokenCopyOfTargetedSpellPermanentEffect()` / `(CreateTokenCopyOfTargetPermanentEffect copyEffect)` — "
              "\"create a token that's a copy of one of those permanents / of that artifact or creature\" of a cast "
              "trigger, the permanents the stack entry's triggering spell targets; which spells trigger it is the "
              "trigger's filter (`Triggers.youCastSpell().withFilter(...).withStackEntryFilter(...)`). The no-argument "
              "form is Orvar, the All-Form's \"if it targets one or more other permanents you control\" (the "
              "intervening \"if\" a `StackEntryTargetsPermanentPredicate` filter of another permanent you control; the "
              "effect reads the spell's targets as it resolves, the live spell or as it left the stack, and copies only "
              "one other than the source still under your control); `(copyEffect)` is Vesuvan Duplimancy's \"a copy of "
              "that artifact or creature, except …\" (the spell's targets as the ability triggered, a target that left "
              "the battlefield copied as it last existed there). The `withSpellSnapshot` form is **Gone** (" + RUN + ").")

EDITS = [
    (QR, "- `FlurryCopyOrDrawTriggerEffect()` -",
     "- `FlurryCopyOrDrawTriggerEffect` — **Gone** (" + RUN + "): flurry's \"copy that spell if it targets a permanent "
     "or player … If you don't copy a spell this way, draw a card\" (Shiko and Narset, Unified) is " + FLURRY
     + ": the \"if\" is read as the ability resolves from the triggering spell, live or as it last existed on the "
     "stack.", "line"),
    (QR, "- `CreateTokenCopyOfTargetedSpellPermanentEffect()` —", TOKEN_COPY, "line"),
    (QR, "`SpellweaverVoluteEnterEffect()` + `SpellweaverVoluteTriggerEffect()` + `CopyEnchantedInstantAndMayCastCopyEffect(UUID)` -",
     VOLUTE + " - attach Spellweaver Volute to an instant card in a graveyard, copy the card it enchants (as the ability "
     "resolves, or as the Aura last existed on the battlefield) when you cast a sorcery, and offer to cast the copy for "
     "free; a successful cast exiles the original and prompts for another instant-card graveyard attachment. "
     "`SpellweaverVoluteTriggerEffect` is **Gone** (" + RUN + ").", "line"),
    (QR, "- `GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect()` —",
     "- `GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect` — **Gone** (" + RUN + "): Loki, God of Lies is "
     + LOKI + "; \"that creature\" is the creature the spell targeted as the ability triggered.", "line"),
    (QR, "- `GainControlOfAllPermanentsMatchingEffect(new PermanentIsTargetOfTriggeringSpellPredicate())` —",
     "", "fragment:the permanents the triggering spell targets, on the stack or as it was when the ability triggered "
     "(Dack Fayden's emblem)=>the permanents the triggering spell targeted as the ability triggered, a target it changed "
     "to later not among them (Dack Fayden's emblem; Loki, God of Lies' \"that creature\" with "
     "`StackEntryTargetsOnlySingleCreaturePredicate`). A `ConditionalEffect` or `SequenceEffect` rider in `thenEffects` "
     "is unwrapped as it resolves (\"if it's your turn, untap that creature\")"),
    (QR, "- `ConditionalEffect(new SpellCounteredThisWay(), wrapped)` —",
     "- `ConditionalEffect(new NotCondition(new SpellCopiedThisWay()), wrapped)` — \"if you don't copy a spell this "
     "way\": met only when no `CopyTriggeringSpellEffect` step earlier in the same entry copied its spell "
     "(`ResolutionResults.copiedSpell`, recorded by `CopyTriggeringSpellEffectHandler`; a spell that can't be copied "
     "records none). Place the copy first (Shiko and Narset, Unified's flurry).", "after"),
    (QR, "- `ConditionalEffect(new SpellCounteredThisWay(), wrapped)` —",
     "- `ConditionalEffect(new TriggeringSpellMatches(StackEntryPredicate), wrapped)` — an \"if\" of a cast trigger's "
     "effect on that spell (\"copy that spell if it targets a permanent or player\"), judged as it resolves against "
     "the stack entry's triggering spell (live, or as it last existed on the stack, CR 608.2h) by "
     "`EffectResolutionService`, from the ability's controller and source. Top-level effects of the ability only; a "
     "condition of the cast itself is the trigger's `withStackEntryFilter`.", "after"),
    (IX, "| `FlurryCopyOrDrawTriggerEffect` |",
     "| `FlurryCopyOrDrawTriggerEffect` | — | **Gone** (" + RUN + "): Shiko and Narset, Unified writes " + FLURRY + " |",
     "line"),
    (IX, "| `SpellweaverVoluteTriggerEffect` |",
     "| `SpellweaverVoluteTriggerEffect` | — | **Gone** (" + RUN + "): Spellweaver Volute writes " + VOLUTE + " |", "line"),
    (IX, "| `CopyEnchantedInstantAndMayCastCopyEffect` |",
     "| `CopyEnchantedInstantAndMayCastCopyEffect` | `()` | Copies the instant card Spellweaver Volute enchants as the "
     "ability resolves (or enchanted as the Aura last existed on the battlefield) and offers to cast the copy without "
     "paying its mana cost; the `(UUID enchantedCardId)` form is only the copy's may ability, which the handler queues. |",
     "line"),
    (IX, "`CreateTokenCopyOfTargetedSpellPermanentEffect` is an `Triggers.youCastSpell()` marker:", TOKEN_COPY[2:], "line"),
    (IX, "| `GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect` |",
     "| `GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect` | — | **Gone** (" + RUN + "): Loki, God of Lies "
     "writes " + LOKI + " |", "line"),
    (IX, "| `ConditionalEffect(new SpellCounteredThisWay(), wrapped)` |",
     "| `ConditionalEffect(new NotCondition(new SpellCopiedThisWay()), wrapped)` | see construction shape | \"If you "
     "don't copy a spell this way\": met only when no `CopyTriggeringSpellEffect` step earlier in the same entry copied "
     "its spell (`ResolutionResults.copiedSpell`). Place the copy first (Shiko and Narset, Unified). |", "after"),
    (IX, "| `TriggeringCardMatches` (condition) |",
     "| `TriggeringSpellMatches` (condition) | `(StackEntryPredicate filter)` | `ConditionalEffect(new "
     "TriggeringSpellMatches(p), effect)`: an \"if\" of a cast trigger's effect on the triggering spell, judged as it "
     "resolves (the live spell, else as it last existed on the stack) from the ability's controller and source "
     "(\"copy that spell if it targets a permanent or player\", Shiko and Narset, Unified). Top-level effects only; a "
     "condition of the cast is the trigger's `withStackEntryFilter(...)` |", "after"),
    (OM, "| \"Whenever you cast a spell that targets only a single creature, gain control of that creature until end of turn.",
     "| \"Whenever you cast a spell that targets only a single creature, gain control of that creature until end of "
     "turn. If it's your turn, untap that creature and it gains haste until end of turn.\" | " + LOKI + " | "
     "Triggers.youCastSpell() | Loki, God of Lies (MSC 512); \"that creature\" is the creature the spell targeted as "
     "the ability triggered. |", "line"),
    (OM, "| \"Whenever you cast a spell that targets only a single artifact or creature you control, create a token",
     "| \"Whenever you cast a spell that targets only a single artifact or creature you control, create a token that's "
     "a copy of that artifact or creature, except it's not legendary\" | " + VESUVAN + " | Triggers.youCastSpell() | "
     "Vesuvan Duplimancy (DMU 73); the spell's original target, as it last existed if it left |", "line"),
    (OM, "| \"Enchant instant card in a graveyard. Whenever you cast a sorcery spell, copy the enchanted instant card.",
     "", "fragment:`SpellweaverVoluteEnterEffect()` + `SpellweaverVoluteTriggerEffect()` with "
     "`CopyEnchantedInstantAndMayCastCopyEffect(UUID)`=>" + VOLUTE),
    (OM, "| \"Whenever you cast a spell that targets only a single creature, gain control of that creature until end of",
     "| \"Flurry — Whenever you cast your second spell each turn, copy that spell if it targets a permanent or player, "
     "and you may choose new targets for the copy. If you don't copy a spell this way, draw a card.\" | " + FLURRY
     + " | Triggers.youCastSpell() | Shiko and Narset, Unified (TDC 7) |", "after"),
    (PR, "| `PermanentIsTargetOfTriggeringSpellPredicate` |",
     "| `PermanentIsTargetOfTriggeringSpellPredicate` | `()` | a permanent the spell that triggered the resolving "
     "ability targeted as the ability triggered (\"gain control of those permanents\", Dack Fayden's emblem; \"gain "
     "control of that creature\", Loki, God of Lies; with `GainControlOfAllPermanentsMatchingEffect`) | "
     "`triggeringSpellTargetIds` |", "line"),
]

EDITS.append(("agent-docs/ARCHITECTURE.md", "A spell-cast trigger's \"that spell\"", "",
              "fragment:the demonstrate self-cast trigger, Flurry's copy-or-draw)=>the demonstrate self-cast trigger)"))
EDITS.append(("agent-docs/ARCHITECTURE.md", "A spell-cast trigger's \"that spell\"", "",
              "fragment: A \"that many\" count of the spell's targets=> An \"if\" of the effect on that spell (\"copy that "
              "spell if it targets a permanent or player\", `TriggeringSpellMatches`) is judged as the ability resolves "
              "by `EffectResolutionService` against the same spell; \"that creature\" / \"those permanents\" the spell "
              "targets (`PermanentIsTargetOfTriggeringSpellPredicate`, Loki, God of Lies, Dack Fayden's emblem) and "
              "Vesuvan Duplimancy's \"that artifact or creature\" are its targets as the ability triggered, a later "
              "change of target not moving them. A \"that many\" count of the spell's targets"))

GONE = ["GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect", "SpellweaverVoluteTriggerEffect",
        "FlurryCopyOrDrawTriggerEffect", "withSpellSnapshot"]


def apply(lines, start, new, mode):
    idx = [i for i, line in enumerate(lines) if line.startswith(start)]
    if mode == "after":
        if new in lines:
            return False
        if not idx:
            raise SystemExit(f"anchor not found: {start}")
        lines.insert(idx[0] + 1, new)
        return True
    if mode == "delete":
        if not idx:
            return False
        del lines[idx[0]]
        return True
    if mode.startswith("fragment:"):
        old, repl = mode[len("fragment:"):].split("=>", 1)
        if any(repl in line for line in lines) and not any(old in lines[i] for i in idx):
            return False
        for i in idx:
            if repl in lines[i]:
                return False
            if old in lines[i]:
                lines[i] = lines[i].replace(old, repl)
                return True
        raise SystemExit(f"fragment not found under {start}: {old}")
    if new in lines:
        return False
    if not idx:
        raise SystemExit(f"line not found: {start}")
    lines[idx[0]] = new
    return True


def main():
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
