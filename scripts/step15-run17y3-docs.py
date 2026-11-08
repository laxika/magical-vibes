#!/usr/bin/env python3
"""Step 15, run 17y3: the agent-docs name the "a permanent deals damage to you", "a creature deals combat damage to
you", "a source of the chosen color deals damage to you" and "an instant or sorcery spell that targets only a single
creature deals damage to that creature" abilities as trigger filters with existing effects.

Whole-line edits, each keyed on the line's start: the removed records' rows become "Gone" rows, the patterns and
Oracle-text rows name the new form (`ReturnToHandEffect.triggering()`, `Triggers.creatureDealsCombatDamageToYou()` with
`DamageSourceControllerGainsControlOfDamagedPermanentEffect()` / `.attackingPlayer()` and
`ExileTargetPermanentUntilSourceLeavesEffect()` behind an intervening "if", the new `Triggers.sourceDealsDamageToYou()`
and `instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature()`), and new rows are added after an anchor line.
Fails if a doc or skill still names a removed record outside a "Gone" line. Idempotent.

Usage: step15-run17y3-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
RUN = "effect-slot-refactor.md step 15, run 17y3"

BOUNCE = "`Triggers.permanentDealsDamageToYou()` + `ReturnToHandEffect.triggering()`"
WAR_ZONE = "`Triggers.creatureDealsCombatDamageToYou()` + `DamageSourceControllerGainsControlOfDamagedPermanentEffect()`"
GAME_BALL = ("`Triggers.creatureDealsCombatDamageToYou()`, `TriggerOptions.ONE_OR_MORE`, "
             "`DamageSourceControllerGainsControlOfDamagedPermanentEffect.attackingPlayer()` + "
             "`UntapPermanentsEffect(TapUntapScope.SELF)`")
HIXUS = ("`Triggers.creatureDealsCombatDamageToYou()`, `TriggerOptions.DEFAULT.withInterveningIf(new "
         "SourceEnteredThisTurn())`, `ExileTargetPermanentUntilSourceLeavesEffect()`")
IMODANE = ("`Triggers.instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature()` + "
           "`DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OPPONENT)`")
CIRCLE = ("`Triggers.sourceDealsDamageToYou().withFilter(new PermanentHasSourceChosenColorPredicate())"
          ".withSpellFilter(new CardHasSourceChosenColorPredicate())` + `MayPayManaEffect(\"{1}\", "
          "SequenceEffect.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(1)), prompt)`")
COMBAT_TO_YOU = ("the damaging creature is the entry's `targetId` (`nonTargeting`) and its triggering permanent; the "
                 "ability goes through the collector's dispatch, so \"triggers an additional time\" applies")


def gone(name, what):
    return f"| `{name}` | (gone) | **Gone** ({RUN}): {what} |"


EDITS = {
    "EFFECTS_INDEX.md": [
        ("| `DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect` |",
         gone("DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect",
              f"\"whenever an instant or sorcery spell you control that targets only a single creature deals damage to "
              f"that creature, ~ deals that much damage to each opponent\" is {IMODANE}: the creature is the entry's "
              f"triggering permanent, the damage dealt to it the event value (Imodane, the Pyrohammer)")),
        ("| `DamageSourceControllerGainsControlOfDamagedPermanentEffect` |",
         "| `DamageSourceControllerGainsControlOfDamagedPermanentEffect` | `()` / `attackingPlayer()` | that source's "
         "controller gains permanent control of this permanent (Crag Saurian on `Triggers.dealtDamage()` with "
         "`TriggerOptions.EACH_SOURCE`, Contested War Zone on `Triggers.creatureDealsCombatDamageToYou()`): the controller "
         "of the stack entry's triggering permanent (the damage source) as the ability resolves, or its last known "
         "controller (CR 608.2h), or the controller the entry recorded for a source that was never a permanent "
         "(`GameQueryService.findTriggeringPermanentController`). `attackingPlayer()`: \"the attacking player gains control "
         "of this artifact\", the active player as the ability resolves (Contested Game Ball, with `UntapPermanentsEffect("
         "TapUntapScope.SELF)` for \"and untaps it\") |"),
        ("| `ReturnDamageSourcePermanentToHandEffect` |",
         gone("ReturnDamageSourcePermanentToHandEffect",
              f"\"whenever a permanent deals damage to you, return it to its owner's hand\" is {BOUNCE}: the permanent is "
              f"the entry's triggering permanent (Dissipation Field)")),
        ("| `DamageSourceControllerGainsControlOfThisPermanentEffect` |",
         gone("DamageSourceControllerGainsControlOfThisPermanentEffect",
              f"\"whenever a creature deals combat damage to you, that creature's controller gains control of this\" is "
              f"{WAR_ZONE} (Contested War Zone); \"whenever you're dealt combat damage, the attacking player gains control "
              f"of this artifact and untaps it\" is {GAME_BALL} (Contested Game Ball)")),
        ("| `ExileDamageSourcePermanentUntilSourceLeavesEffect` |",
         gone("ExileDamageSourcePermanentUntilSourceLeavesEffect",
              f"\"whenever a creature deals combat damage to you, if this entered this turn, exile that creature until "
              f"this leaves the battlefield\" is {HIXUS}: {COMBAT_TO_YOU}; the intervening \"if\" is judged as the ability "
              f"triggers and again as it resolves (Hixus, Prison Warden)")),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Whenever an instant or sorcery spell you control that targets only a single creature deals damage to that creature, ~ deals that much damage to each opponent\" |",
         f"| \"Whenever an instant or sorcery spell you control that targets only a single creature deals damage to that "
         f"creature, ~ deals that much damage to each opponent\" | `DealDamageToPlayersEffect(new EventValue(), "
         f"DamageRecipient.EACH_OPPONENT)` | `Triggers.instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature()` | "
         f"Imodane, the Pyrohammer (WOE 137). The damage batch keeps the spell's sole creature target; the collector puts "
         f"the ability on the stack with that creature as its triggering permanent and the damage dealt to it as the "
         f"event value |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("  A wrapped `DamageSourceControllerGainsControlOfThisPermanentEffect` captures the attacking player",
         f"  Contested Game Ball is {GAME_BALL}: the attacking player is read as the ability resolves, including separate "
         f"first-strike and normal-damage triggers. Redirected combat damage to the attacking player still triggers the "
         f"ability and untaps its source."),
        ("- `DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect()` —",
         f"- `DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect()` — **Gone** ({RUN}): Imodane is "
         f"{IMODANE}; the collector puts the ability on the stack when the spell's only target is a creature dealt "
         f"damage, that creature its triggering permanent and the damage dealt to it the event value"),
        ("- `ExileDamageSourcePermanentUntilSourceLeavesEffect(",
         f"- `ExileDamageSourcePermanentUntilSourceLeavesEffect` — **Gone** ({RUN}): Hixus, Prison Warden is {HIXUS}; "
         f"{COMBAT_TO_YOU}"),
        ("`DamageSourceControllerGainsControlOfDamagedPermanentEffect()` — on `Triggers.dealtDamage()`",
         "`DamageSourceControllerGainsControlOfDamagedPermanentEffect()` — on `Triggers.dealtDamage()` with "
         "`TriggerOptions.EACH_SOURCE`: that source's controller (as the ability resolves, CR 608.2h) gains permanent "
         "control of this creature (Crag Saurian); on `Triggers.creatureDealsCombatDamageToYou()` that creature's "
         "controller gains control of this land (Contested War Zone). `.attackingPlayer()` gives it to the attacking "
         "player instead (Contested Game Ball)."),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| Whenever an instant/sorcery you control damages a single targeted creature |",
         f"| Whenever an instant/sorcery you control damages a single targeted creature | `i/ImodaneThePyrohammer.java` | "
         f"{IMODANE} — the spell's only target is a creature dealt damage; that much is dealt to each opponent |"),
        ("| Damage-to-controller bounce |",
         f"| Damage-to-controller bounce | `d/DissipationField.java` | {BOUNCE} — bounces any permanent that deals "
         f"damage to controller, the entry's triggering permanent |"),
        ("| Damage-to-controller steal |",
         f"| Damage-to-controller steal | `c/ContestedWarZone.java` | {WAR_ZONE} — combat damage from a creature gives "
         f"that creature's controller (as the ability resolves) control of this land |"),
        ("| Damage-to-controller exile until this leaves |",
         f"| Damage-to-controller exile until this leaves | `h/HixusPrisonWarden.java` | {HIXUS} — O-ring exile of the "
         f"damaging creature; the intervening \"if\" is judged as the ability triggers and again as it resolves "
         f"(CR 603.4) |"),
        ("| Combat-damage-to-you revenge + token |",
         "| Combat-damage-to-you revenge + token | `t/TeysaEnvoyOfGhosts.java` | Triggers.creatureDealsCombatDamageToYou() "
         "DestroyTargetPermanentEffect() + CreateTokenEffect(...) — \"whenever a creature deals combat damage to you, "
         "destroy that creature. Create a token.\" Both effects share the one stack entry whose baked-in `targetId` is the "
         "damaging creature (also its triggering permanent); creature- and combat-only, unlike "
         "Triggers.permanentDealsDamageToYou() |"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("| `Triggers.creatureDealsCombatDamageToYou()` |",
         "| `Triggers.creatureDealsCombatDamageToYou()` | `TriggerCollectionService.queueCreatureCombatDamageToYouTriggers` "
         "— never targets; the damaging creature is baked in as the stack entry's `targetId` with `nonTargeting = true` "
         "and is its triggering permanent, so no pending-choice is queued. The entry goes through the collector's "
         "dispatch and `enqueueTrigger`. Teysa, Envoy of Ghosts; Hixus, Prison Warden. | None (baked-in subject) |"),
    ],
}

INSERTS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"whenever a red creature or spell deals damage, this enchantment deals that much damage to that creature's or spell's controller\" |",
         f"| \"As this enchantment enters, choose a color. Whenever a source of the chosen color deals damage to you, you may "
         f"pay {{1}}. If you do, target player loses 1 life and you gain 1 life.\" | `STATIC ChooseColorOnEnterEffect()` + "
         f"{CIRCLE} | `Triggers.sourceDealsDamageToYou()` | Circle of Affliction (PLC 66) — once for each source that deals "
         f"damage to you, the damage dealt to you the event value; the source's own colors count, so Ghostly Flame doesn't "
         f"stop it; the target is chosen as the ability goes on the stack |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `Triggers.sourceDealsDamage().withFilter(new PermanentHasSubtypePredicate(CardSubtype.SLIVER))",
         f"- {CIRCLE} — \"whenever a source of the chosen color deals damage to you\" (Circle of Affliction): once for each "
         f"source, the damage dealt to you the event value; the target is chosen as the ability goes on the stack"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("| `Triggers.sourceDealsDamage()` / `sourceYouControlDealsDamage()` |",
         "| `Triggers.sourceDealsDamageToYou()` | \"Whenever a source deals damage to you, ...\" — any source, a permanent or "
         "not, once for each source that deals damage to you (Circle of Affliction's ruling); filtered as "
         "`Triggers.sourceDealsDamage()` is (`withFilter` the source as a permanent, `withSpellFilter` as a spell or card: "
         "\"a source of the chosen color\" is `PermanentHasSourceChosenColorPredicate` / `CardHasSourceChosenColorPredicate`); "
         "the damage dealt to you is the event value, and a target is chosen as the ability goes on the stack. Circle of "
         "Affliction |"),
        ("| `Triggers.instantOrSorceryYouControlDealsDamage()` |",
         "| `Triggers.instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature()` | \"Whenever an instant or sorcery "
         "spell you control that targets only a single creature deals damage to that creature, ...\" — that creature is "
         "the stack entry's triggering permanent and the damage dealt to it the event value. Imodane, the Pyrohammer |"),
    ],
}

REMOVED = ["ReturnDamageSourcePermanentToHandEffect", "DamageSourceControllerGainsControlOfThisPermanentEffect",
           "ExileDamageSourcePermanentUntilSourceLeavesEffect",
           "DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect"]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        lines = path.read_text().split("\n")
        for prefix, new in edits:
            if new in lines:
                continue
            hits = [i for i, line in enumerate(lines) if line.startswith(prefix)]
            if len(hits) != 1:
                raise SystemExit(f"{name}: {len(hits)} lines start with {prefix!r}")
            lines[hits[0]] = new
        path.write_text("\n".join(lines))
    for name, inserts in INSERTS.items():
        path = DOCS / name
        lines = path.read_text().split("\n")
        for anchor, new in inserts:
            if new in lines:
                continue
            hits = [i for i, line in enumerate(lines) if line.startswith(anchor)]
            if len(hits) < 1:
                raise SystemExit(f"{name}: no line starts with {anchor!r}")
            lines.insert(hits[0] + 1, new)
        path.write_text("\n".join(lines))
    paths = list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md"))
    for path in paths:
        for line in path.read_text().split("\n"):
            for removed in REMOVED:
                if re.search(r"\b" + removed + r"\b", line) and "**Gone**" not in line:
                    raise SystemExit(f"{path.name} still names {removed}: {line[:120]}")


if __name__ == "__main__":
    main()
