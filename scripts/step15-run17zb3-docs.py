#!/usr/bin/env python3
"""Step 15, run 17zb3: the agent-docs lines that name the removed cast markers of Harness the Storm, Toofer, Keeper of
the Full Grip and Spellweaver Helix, the changed forms of `CopyImprintedCardAndMayCastCopyEffect` and
`TrackCastSpellCardTypesEffect`, the new trigger `Triggers.yourSpellCausesYouToGainCardAdvantage()`, the new predicate
`StackEntryHasTypeShowingOnSourceTrackerPredicate`, `TriggeringSpellMatches` as an intervening "if" and
`CardSharesNameWithTriggeringSpellPredicate` as a graveyard target's restriction.

Whole-line edits keyed by the line's start (the old line is replaced, or a new line inserted after an anchor line),
or a fragment replaced inside a line, so a second run changes nothing. Fails if a doc or skill still names a removed
marker outside a "Gone" line.

Usage: step15-run17zb3-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RUN = "effect-slot-refactor.md step 15, run 17zb3"
QR = "agent-docs/EFFECTS_QUICK_REFERENCE.md"
IX = "agent-docs/EFFECTS_INDEX.md"
OM = "agent-docs/ORACLE_TEXT_EFFECT_MAP.md"
PR = "agent-docs/PREDICATES_REFERENCE.md"
CP = "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md"
TS = "agent-docs/TRIGGER_SLOT_TARGETING.md"
AG = "agent-docs/ACTIVATED_ABILITY_GUIDE.md"

HARNESS = ("`Triggers.youCastSpell().withFilter(instant or sorcery).castFrom(Zone.HAND)` + "
           "`CastTargetInstantOrSorceryFromGraveyardEffect(GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, "
           "new CardSharesNameWithTriggeringSpellPredicate())`")
TOOFER = ("`Triggers.yourSpellCausesYouToGainCardAdvantage()` + `LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)`")
HELIX = ("`Triggers.playerCastsSpell().withStackEntryFilter(new StackEntryNotPredicate(new StackEntryIsCopyPredicate("
         "true)))`, `TriggerOptions.DEFAULT.withInterveningIf(new TriggeringSpellMatches(new "
         "StackEntrySharesNameWithCardExiledWithSourcePredicate()))` + `new MayEffect("
         "CopyImprintedCardAndMayCastCopyEffect.otherExiledCard(), \"…\")`")
BUCKET = ("`Triggers.youCastSpell().withStackEntryFilter(new StackEntryHasTypeShowingOnSourceTrackerPredicate(types))` + "
          "`TrackCastSpellCardTypesEffect(types)`")
CARD_ADVANTAGE = ("\"whenever a spell you control causes you to gain card advantage\" is an event of the spell's "
                  "resolution: each spell records every player's cards in hand and nontoken permanents as it begins to "
                  "resolve (its own card in its owner's hand when cast from there), and as it finishes resolving its "
                  "controller gains card advantage when their gain is greater than their opponents' together; then "
                  "`TriggerCollectionService.checkSpellCardAdvantageTriggers` puts each such ability of a permanent the "
                  "spell's controller controls on the stack, the spell as its triggering spell. A copy counts; an "
                  "ability doesn't")

EDITS = [
    (QR, "- `CardAdvantageSpellCastTriggerEffect()` —",
     "- `CardAdvantageSpellCastTriggerEffect` — **Gone** (" + RUN + "): Toofer, Keeper of the Full Grip is " + TOOFER
     + "; " + CARD_ADVANTAGE + ".", "line"),
    (QR, "- `CastSameNameCardFromGraveyardOnSpellCastEffect()` —",
     "- `CastSameNameCardFromGraveyardOnSpellCastEffect` — **Gone** (" + RUN + "): Harness the Storm is " + HARNESS
     + "; the graveyard target is chosen against the triggering spell's name as the ability goes on the stack "
     "(`SpellGraveyardTargetTrigger.triggeringSpell`) and checked again, and offered for its normal cost, against the "
     "spell as it resolves (the live spell, else as it last existed on the stack).", "line"),
    (QR, "- `SpellweaverHelixTriggerEffect()` —",
     "- `SpellweaverHelixTriggerEffect` — **Gone** (" + RUN + "): Spellweaver Helix is " + HELIX + "; the "
     "intervening \"if\" is judged on the spell as it is cast and again as the ability resolves, and \"the other\" is "
     "every card exiled with the source but the first with the spell's name, read as it resolves.", "line"),
    (QR, "- `ConditionalEffect(new TriggeringSpellMatches(StackEntryPredicate), wrapped)` —", "",
     "fragment:a condition of the cast itself is the trigger's `withStackEntryFilter`.=>a condition of the cast itself "
     "is the trigger's `withStackEntryFilter`. As a cast trigger's intervening \"if\" "
     "(`TriggerOptions.DEFAULT.withInterveningIf(new TriggeringSpellMatches(p))`) the cast collector judges it on the "
     "spell as it is cast, from the ability's source, and again as the ability resolves (Spellweaver Helix's \"if it "
     "has the same name as one of the cards exiled with this artifact\")."),
    (IX, "| `CardAdvantageSpellCastTriggerEffect` |",
     "| `CardAdvantageSpellCastTriggerEffect` | — | **Gone** (" + RUN + "): Toofer, Keeper of the Full Grip writes "
     + TOOFER + " |", "line"),
    (IX, "| `CastSameNameCardFromGraveyardOnSpellCastEffect` |",
     "| `CastSameNameCardFromGraveyardOnSpellCastEffect` | — | **Gone** (" + RUN + "): Harness the Storm writes "
     + HARNESS + " |", "line"),
    (IX, "| `SpellweaverHelixTriggerEffect` |",
     "| `SpellweaverHelixTriggerEffect` | — | **Gone** (" + RUN + "): Spellweaver Helix writes " + HELIX + " |", "line"),
    (IX, "| `CopyImprintedCardAndMayCastCopyEffect` |", "",
     "fragment:The `otherExiledCard(UUID)` factory takes the matching exiled card ID and selects the other of two "
     "source-tracked cards for Spellweaver Helix; it does not depend on the triggering spell remaining on the stack.=>"
     "The `otherExiledCard()` factory is Spellweaver Helix's \"copy the other\": every card exiled with the source but "
     "the first with the triggering spell's name, read as it resolves (the live spell, else as it last existed on the "
     "stack), and nothing when none has it."),
    (IX, "| `TriggeringSpellMatches` (condition) |", "",
     "fragment:Top-level effects only; a condition of the cast is the trigger's `withStackEntryFilter(...)`=>Top-level "
     "effects only; a condition of the cast is the trigger's `withStackEntryFilter(...)`. As a cast trigger's "
     "intervening \"if\" (`TriggerOptions.DEFAULT.withInterveningIf(...)`) the cast collector judges it on the spell as "
     "it is cast and again as it resolves (Spellweaver Helix)"),
    (IX, "`TrackCastSpellCardTypesEffect` checks spell types",
     "`TrackCastSpellCardTypesEffect(Set<CardType> trackedTypes)` (Bucket List) marks the tracked types of the stack "
     "entry's triggering spell, as it was when the ability triggered, on a tracker list associated with the source, "
     "independently of permanent counters (the source live, else its snapshot as the ability triggered). It draws once "
     "for each spell adding a new type, then sacrifices the source and draws once more if the list completes. \"A "
     "spell of a type showing\" is the trigger's `withStackEntryFilter(new "
     "StackEntryHasTypeShowingOnSourceTrackerPredicate(trackedTypes))`. A condition of the trigger event is "
     "`TriggerOptions.withEventCondition(condition)`, checked as the ability triggers and not again on resolution "
     "(CR 603.2).", "line"),
    (OM, "| \"Whenever a spell you control causes you to gain card advantage, each opponent loses 2 life.\" |",
     "| \"Whenever a spell you control causes you to gain card advantage, each opponent loses 2 life.\" | "
     "`LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)` | `Triggers.yourSpellCausesYouToGainCardAdvantage()` | "
     "Toofer, Keeper of the Full Grip (MB2 318/554); " + CARD_ADVANTAGE + ". Tokens aren't cards. |", "line"),
    (OM, "| \"Whenever a spell you control causes you to gain card advantage, each opponent loses 2 life.\" |",
     "| \"Whenever you cast a spell of a type showing on [this], put a counter over that type and draw a card. If all "
     "five types on [this] have counters over them, sacrifice it and draw one more card.\" | " + BUCKET + " | "
     "Triggers.youCastSpell() | Bucket List (MB1 89); the tracker list belongs to the permanent, so a spell of no "
     "showing type doesn't trigger it |", "after"),
    (OM, "| \"Whenever you cast an instant or sorcery spell from your hand, you may cast target card with the same name "
     "as that spell from your graveyard. (You still pay its costs.)\" |",
     "| \"Whenever you cast an instant or sorcery spell from your hand, you may cast target card with the same name "
     "as that spell from your graveyard. (You still pay its costs.)\" | " + HARNESS + " | `Triggers.youCastSpell()` | "
     "Harness the Storm (SOI). The target is chosen against the triggering spell's name as the ability goes on the "
     "stack; a spell countered in response is read as it last existed on the stack |", "line"),
    (OM, "| \"Whenever you cast an instant or sorcery spell from your hand, you may cast target card with the same name "
     "as that spell from your graveyard. (You still pay its costs.)\" |",
     "| \"Whenever a player casts a card, if it has the same name as one of the cards exiled with this artifact, you "
     "may copy the other. If you do, you may cast the copy without paying its mana cost.\" | " + HELIX + " | "
     "Triggers.playerCastsSpell() | Spellweaver Helix (MRD 247); a cast copy isn't a card |", "after"),
    (PR, "| `CardSharesNameWithTriggeringSpellPredicate` |", "",
     "fragment:; matches nothing elsewhere=>; `EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect`'s filter "
     "(Bloodbond March) and `CastTargetInstantOrSorceryFromGraveyardEffect`'s (Harness the Storm's \"target card with "
     "the same name as that spell\", chosen against the spell the ability triggered on as it goes on the stack and "
     "checked again as it resolves) read it through `AmountEvaluationService.bindTriggeringSpellName`; it matches "
     "nothing elsewhere"),
    (PR, "| `StackEntrySharesNameWithCardExiledWithSourcePredicate` |",
     "| `StackEntryHasTypeShowingOnSourceTrackerPredicate` | `(Set<CardType> trackedTypes)` | a spell with a card type "
     "among `trackedTypes` that has no counter over it on the source permanent's tracker list (\"a spell of a type "
     "showing on Bucket List\"); a tracker list kept for another card is empty. Source-dependent: matches nothing "
     "without a source |", "after"),
    (CP, "| Same-name graveyard instant/sorcery cast on hand spell |",
     "| Same-name graveyard instant/sorcery cast on hand spell | `h/HarnessTheStorm.java` | " + HARNESS + " — the "
     "generic cast collector chooses the graveyard target against the triggering spell's name as the ability goes on "
     "the stack; the graveyard-cast effect offers the normal-cost cast as it resolves |", "line"),
    (TS, "`Triggers.youCastSpell()` same-name graveyard casts",
     "`Triggers.youCastSpell()` graveyard targets restricted by the triggering spell (\"target card with the same name "
     "as that spell\", Harness the Storm's `CardSharesNameWithTriggeringSpellPredicate` in the effect's filter) are "
     "chosen through the generic `SpellGraveyardTargetTrigger`, which carries the triggering spell "
     "(`withTriggeringSpell`) so the choice binds its name; the graveyard target operation and the entry keep it for the "
     "check as the ability resolves.", "line"),
    (AG, "| `Triggers.youCopySpell()` |",
     "| `Triggers.yourSpellCausesYouToGainCardAdvantage()` | A spell the controller controls finishes resolving and "
     "gives them card advantage (Toofer, Keeper of the Full Grip): read by its own reader, not by an effect's class |",
     "after"),
]

EDITS.append(("agent-docs/ARCHITECTURE.md", "A spell-cast trigger's \"that spell\"", "",
              "fragment:A graveyard target restricted by the spell's mana value is chosen=>A graveyard target "
              "restricted by the spell's name (`CardSharesNameWithTriggeringSpellPredicate`, Harness the Storm) is "
              "chosen against the spell `SpellGraveyardTargetTrigger.triggeringSpell` carries, which the graveyard "
              "target operation keeps for the entry and its check as it resolves; an intervening \"if\" on the spell "
              "(`TriggeringSpellMatches` as `TriggerOptions.withInterveningIf`, Spellweaver Helix) is judged on it as it "
              "is cast and again as the ability resolves. \"Whenever a spell you control causes you to gain card "
              "advantage\" (Toofer, Keeper of the Full Grip) is an event of the spell's resolution: the spell records "
              "every player's card count on its entry as it begins to resolve (`StackEntry.cardCountsBeforeResolution`), "
              "and `StackResolutionService` judges it as the spell finishes resolving and collects the abilities with "
              "the spell as their triggering spell. A graveyard target restricted by the spell's mana value is chosen"))

GONE = ["CardAdvantageSpellCastTriggerEffect", "CastSameNameCardFromGraveyardOnSpellCastEffect",
        "SpellweaverHelixTriggerEffect", "matchingExiledCardId"]


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
