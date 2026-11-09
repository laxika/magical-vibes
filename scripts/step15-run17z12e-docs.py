#!/usr/bin/env python3
"""Step 15, run 17z12e: agent-docs follow the run (the seek collector's record and Lurker in the Deep's event condition,
the "choose up to X" mode count on the choice, the delayed token's event value, the source's attachments as it left)."""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"


def edit(name, pairs):
    path = DOCS / name
    src = path.read_text()
    for old, new in pairs:
        if src.count(old) != 1:
            sys.exit(f"{name}: expected one match of\n{old}")
        src = src.replace(old, new)
    path.write_text(src)
    print("updated", name)


edit("EFFECTS_INDEX.md", [
    ("| `ConjureDuplicatesOfSoughtCardsAndManifestEffect` | `()` or `(List<Card> soughtCards)` | conjures fresh duplicates "
     "of cards sought during the triggering event, then manifests those duplicates. Used by Lurker in the Deep (`YDSK` 6) |",
     "| `ConjureDuplicatesOfSoughtCardsAndManifestEffect` | `()` | conjures fresh duplicates of the cards the seek event was "
     "about (\"those cards\": the stack entry's triggering cards, which the seek collector records), then manifests those "
     "duplicates. Used by Lurker in the Deep (`YDSK` 6), whose \"during your turn\" is its trigger's event condition "
     "(`TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn())`) |"),
    ("has its maximum evaluated by the cast collector against the triggering spell as the ability goes on the stack; a "
     "maximum of 0 offers no mode, and the ability is removed (CR 700.2b).",
     "has its maximum evaluated by the cast collector against the triggering spell as the ability goes on the stack; a "
     "maximum of 0 offers no mode, and the ability is removed (CR 700.2b). The modal goes to the mode choice as the card "
     "wrote it; the number of modes the event set rides on the choice (`TriggeredModalTrigger.withModeCount`), as does "
     "an enters or beginning-of-combat modal's \"choose both instead\" condition judged as the ability triggers."),
    ("registers a delayed trigger that fires at the beginning of the resolving controller's next upkeep, snapshots "
     "dynamic token amounts then, and puts the resulting token effect on the stack.",
     "registers a delayed trigger that fires at the beginning of the resolving controller's next upkeep, snapshots "
     "dynamic token amounts then, and puts the token effect on the stack as written; an `EventValue` amount reads the "
     "registering resolution's event value, which the delayed trigger carries as its own."),
    ("create one token and put one named counter on it for each Aura or Equipment attached to the source permanent, "
     "other than the stack entry's granting permanent (\"other than Animal Friend\").",
     "create one token and put one named counter on it for each Aura or Equipment attached to the source permanent, "
     "other than the stack entry's granting permanent (\"other than Animal Friend\"); once the source has left, the ones "
     "attached to it as it left (`StackEntry.getSourceAttachmentIds()`, recorded for every entry whose source leaves)."),
])
edit("EFFECTS_QUICK_REFERENCE.md", [
    ("- `Triggers.youSeek()` — the trigger of abilities that fire whenever the controller successfully seeks one or more "
     "cards.",
     "- `Triggers.youSeek()` — the trigger of abilities that fire whenever the controller successfully seeks one or more "
     "cards (a seek that found nothing doesn't trigger); the entry records the seeking player as its triggering player "
     "and the sought cards as its triggering cards. \"During your turn\" is "
     "`TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn())`."),
    ("- `ConjureDuplicatesOfSoughtCardsAndManifestEffect()` — conjures fresh duplicates of the cards from a seek event, "
     "then manifests those duplicates; used by Lurker in the Deep (YDSK 6).",
     "- `ConjureDuplicatesOfSoughtCardsAndManifestEffect()` — conjures fresh duplicates of the cards from a seek event "
     "(the stack entry's triggering cards), then manifests those duplicates; used by Lurker in the Deep (YDSK 6)."),
    ("If the token amount is `EventValue`, it is snapshotted when the registrar resolves so a preceding same-entry effect "
     "can supply the count.",
     "If the token amount is `EventValue`, the registrar's event value (a preceding same-entry effect's count) is carried "
     "by the delayed trigger as its own event value; the token effect is not rebuilt."),
])
edit("TRIGGER_SLOT_TARGETING.md", [
    ("| `Triggers.youSeek()` | `SeekTriggerCollectorService` | Controller seeks one or more cards; the sought cards are "
     "carried in `TriggerContext.Seek` and non-targeting effects enqueue directly |",
     "| `Triggers.youSeek()` | `SeekTriggerCollectorService` | Controller seeks one or more cards (no dispatch for a seek "
     "that found nothing); every entry records the seeking player as triggering player and the sought cards as "
     "triggering cards whatever the effects are, and non-targeting effects enqueue directly |"),
])
edit("ORACLE_TEXT_EFFECT_MAP.md", [
    ("+ `Triggers.youSeek() ConjureDuplicatesOfSoughtCardsAndManifestEffect()`;",
     "+ `Triggers.youSeek()` with `TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn())` "
     "`ConjureDuplicatesOfSoughtCardsAndManifestEffect()`;"),
])
edit("ARCHITECTURE.md", [
    ("refreshed as the source leaves the battlefield and carries combat state (`blockingTargetIds`) and",
     "refreshed as the source leaves the battlefield (with the Auras and Equipment attached to it then,\n"
     "`StackEntry.sourceAttachmentIds`, for every entry whatever the effects) and carries combat state "
     "(`blockingTargetIds`) and"),
])
