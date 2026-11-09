#!/usr/bin/env python3
"""Step 15, run 17z7: agent-docs for the death, graveyard, enter, life-gain and discard rewrites that went.

Fragment edits (each old text must occur exactly once, or already be replaced): Lynde's fieldless delayed return,
Purgatory's "put into your graveyard" trigger, the life-gain "target opponent" target filter and the "you gain life"
default, the enter may-pay's "that creature" being referenced rather than targeted, the discard default, and the
equipped-death return through the default. Fails if a doc or skill still names the removed record form outside a
"Gone" line.

Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`RegisterDelayedReturnCurseAttachedToPlayerEffect(null, controllerId)`",
         "`RegisterDelayedReturnCurseAttachedToPlayerEffect()`"),
        ("The death trigger is ownership-based and returns the Curse under its owner's control attached to Lynde's "
         "controller;",
         "The death trigger is ownership-based and returns the Curse under its owner's control attached to the "
         "ability's controller (CR 603.7e); the Curse is the stack entry's triggering card, read as the ability "
         "resolves, and one that left the graveyard and came back is a new object (CR 400.7);"),
        ("| `new ExileTriggeringCreatureAndTrackWithSourceEffect()` | `Triggers.nontokenCreatureDies(YOU_CONTROL)` | "
         "Purgatory. Mandatory (no `MayEffect`);",
         "| `new ExileTriggeringCreatureAndTrackWithSourceEffect()` | `Triggers.dies(Triggers.NONTOKEN_CREATURE, "
         "ObjectRelation.YOU_OWN)` | Purgatory. Mandatory (no `MayEffect`); \"your graveyard\" is the trigger's "
         "`YOU_OWN` relation;"),
        ("Wrap in `MayEffect` on `Triggers.creatureDies(YOU_CONTROL)` for the \"you may exile it\" shape (Colfenor's "
         "Urn) |",
         "Wrap in `MayEffect` on `Triggers.dies(filter, YOU_OWN)` for the \"you may exile it\" shape (Colfenor's "
         "Urn); the \"you may\" is asked as the ability resolves (CR 603.5) |"),
        ("| Monument to Endurance. The discard collector queues the mode choice as the ability goes on the stack, with "
         "the modes the source permanent chose this turn removed;",
         "| Monument to Endurance. The discard collector's default handler puts the ability on the stack, where its "
         "mode is announced (CR 603.3c) with the modes the source permanent chose this turn removed;"),
        ("Firesong and Sunspeaker (DOM 280); \"whenever a [filter] spell causes you to gain life\"",
         "Firesong and Sunspeaker (DOM 280); the \"you gain life\" collector's default handler chooses the target. "
         "\"Whenever a [filter] spell causes you to gain life\""),
        ("| `Triggers.creatureDies(EQUIPPED)` | Oathkeeper, Takeno's Daisho. `attachSource=false` — the Equipment does "
         "not reattach.",
         "| `Triggers.creatureDies(EQUIPPED)` | Oathkeeper, Takeno's Daisho. `attachSource=false` — the Equipment does "
         "not reattach. The equipped-death default puts the ability on the stack as written (a token that dies "
         "triggers it; there is no card to return); the \"if\" reads the card as the ability resolves."),
        ("`EventValue()` for \"equal to the life you gained\" (Sanguine Bond), `PermanentCount(filter, CONTROLLER)` "
         "for \"for each … you control\" (Bishop) |",
         "`EventValue()` for \"equal to the life you gained\" (Sanguine Bond), `PermanentCount(filter, CONTROLLER)` "
         "for \"for each … you control\" (Bishop). A triggered \"target opponent loses …\" declares its target: "
         "`target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT), "
         "\"Target must be an opponent\")).addTrigger(Triggers.youGainLife(), new LoseLifeEffect(new EventValue(), "
         "TARGET_PLAYER))` (Sanguine Bond, Vito, Thorn of the Dusk Rose, Defiant Bloodlord, Enduring Tenacity), chosen "
         "as the ability goes on the stack (CR 603.3d) |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `RegisterDelayedReturnCurseAttachedToPlayerEffect(UUID cardId, UUID attachedPlayerId)` — ally-permanent "
         "graveyard trigger marker: at the beginning of the next end step, return the triggering Curse from its "
         "owner's graveyard under its owner's control attached to the recorded player. Lynde, Cheerful Tormentor.",
         "- `RegisterDelayedReturnCurseAttachedToPlayerEffect()` — \"return it to the battlefield attached to you at "
         "the beginning of the next end step\": the Curse is the stack entry's triggering card (its graveyard stay, "
         "CR 400.7), returned under its owner's control attached to the ability's controller (CR 603.7e). Lynde, "
         "Cheerful Tormentor. Its `(UUID cardId, UUID attachedPlayerId)` form, which the death collector rebuilt, is "
         "**Gone** (effect-slot-refactor.md step 15, run 17z7)."),
        ("`true` also attaches the source Equipment (Nim Deathmantle), `false` just reanimates (Oathkeeper, Takeno's "
         "Daisho), and the optional counter rider is applied as it enters",
         "`true` also attaches the source Equipment (Nim Deathmantle), `false` just reanimates (Oathkeeper, Takeno's "
         "Daisho), and the optional counter rider is applied as it enters. The Equipment is the stack entry's source "
         "permanent; the death collectors put the ability on the stack as written (a \"you may pay\" asked as it "
         "resolves, CR 603.5)"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `RegisterDelayedReturnCurseAttachedToPlayerEffect` | `(UUID cardId, UUID attachedPlayerId)` | registers a "
         "next-end-step return of a triggering Curse from its owner's graveyard to the battlefield attached to the "
         "recorded player (Lynde, Cheerful Tormentor) |",
         "| `RegisterDelayedReturnCurseAttachedToPlayerEffect` | `()` | registers a next-end-step return of the stack "
         "entry's triggering Curse (its graveyard stay, CR 400.7) from its owner's graveyard to the battlefield "
         "attached to the ability's controller (Lynde, Cheerful Tormentor); the `(UUID cardId, UUID "
         "attachedPlayerId)` form is **Gone** (effect-slot-refactor.md step 15, run 17z7) |"),
        ("| `ExileTriggeringCreatureAndTrackWithSourceEffect` | `()` | Triggers.creatureDies(YOU_CONTROL) or "
         "Triggers.nontokenCreatureDies(YOU_CONTROL) resolution:",
         "| `ExileTriggeringCreatureAndTrackWithSourceEffect` | `()` | Triggers.creatureDies(YOU_CONTROL), "
         "Triggers.nontokenCreatureDies(...) or `Triggers.dies(filter, YOU_OWN)` (\"put into your graveyard\") "
         "resolution:"),
        ("Used by Colfenor's Urn (may, toughness-gated) and Purgatory (mandatory, nontoken slot) |",
         "Used by Colfenor's Urn (may, toughness-gated) and Purgatory (mandatory, `Triggers.dies(Triggers"
         ".NONTOKEN_CREATURE, ObjectRelation.YOU_OWN)`) |"),
        ("Tiana's dying Aura or Equipment (`Triggers.dies(Triggers.AURA_OR_EQUIPMENT, ObjectRelation.YOU_CONTROL)`, the "
         "optional form)",
         "Tiana's dying Aura or Equipment (`Triggers.dies(Triggers.AURA_OR_EQUIPMENT, ObjectRelation.YOU_CONTROL)` + "
         "`(true)`, the optional form the card writes)"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| `p/Purgatory.java` | Triggers.nontokenCreatureDies(YOU_CONTROL) ExileTriggeringCreatureAndTrackWithSourceEffect()",
         "| `p/Purgatory.java` | Triggers.dies(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_OWN) "
         "ExileTriggeringCreatureAndTrackWithSourceEffect()"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("     `LifeGainTriggerAnyTarget`'s filterless branch offers the permanents its effect's declared target\n"
         "     admits (\"target creature or player\", `DealDamageToTargetCreatureOrPlayerEffect`: creatures only;\n"
         "     an any-target effect: creatures, planeswalkers and battles); `DrawTriggerAnyTarget` is creature-only.",
         "     `LifeGainTriggerAnyTarget`'s filterless branch offers the permanents its effect's declared target\n"
         "     admits (\"target creature or player\", `DealDamageToTargetCreatureOrPlayerEffect`: creatures only;\n"
         "     an any-target effect: creatures, planeswalkers and battles; a player-only target: none); with the\n"
         "     ability's target filter (\"target opponent\": Sanguine Bond's `target(PlayerPredicateTargetFilter(\n"
         "     opponent))`) it offers what the filter admits, and the stack entry keeps the filter for a copy's new\n"
         "     target. The \"you gain life\" collector's default handler queues it for any targeted effect that no\n"
         "     handler of its own collects; `DrawTriggerAnyTarget` is creature-only."),
        ("| Life-gain (`LifeGainTriggerAnyTarget`) | —                  | ✅ all players | ✅ the effect's declared permanents | ❌ | ❌ | ❌ | ❌ |",
         "| Life-gain (`LifeGainTriggerAnyTarget`) | `Options.ATTACK` with the ability's target filter, else — | ✅ all players, or the filter's | ✅ the effect's declared permanents, or the filter's | ✅ with the filter | ✅ with the filter | ✅ with the filter | ❌ |"),
    ],
}

REMOVED = ["RegisterDelayedReturnCurseAttachedToPlayerEffect(null", "RegisterDelayedReturnCurseAttachedToPlayerEffect(UUID"]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: expected exactly one occurrence of: {old[:100]}")
            text = text.replace(old, new)
        path.write_text(text)
        print(f"{name}: up to date")
    skills = ROOT / ".claude/skills"
    paths = list(DOCS.glob("*.md")) + (list(skills.rglob("*.md")) if skills.is_dir() else [])
    for path in paths:
        for line in path.read_text().splitlines():
            if any(form in line for form in REMOVED) and "**Gone**" not in line:
                raise SystemExit(f"{path}: still names a removed form: {line[:120]}")


if __name__ == "__main__":
    sys.exit(main())
