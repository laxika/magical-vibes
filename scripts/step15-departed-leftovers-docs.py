#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 5: agent-docs lines for the departed-permanent rebuilds that are gone.

Idempotent; run from the repository root:

    python3 scripts/step15-departed-leftovers-docs.py

Each entry replaces (or, with None, deletes) the one line that starts with the given prefix; each insert adds a line
after the one line that starts with its anchor.
"""
import pathlib

PUT_COUNTER = "PutCounterOnTargetForEachDyingSourceCounterEffect"

CATHEDRAL = ("| \"When this creature dies during combat, it deals 6 damage to each creature it blocked this combat.\" | "
             "`new DealDamageToBlockedAttackersOnDeathEffect(6)` with "
             "`TriggerOptions.DEFAULT.withEventCondition(new DuringCombat())` | `Triggers.dies(THIS)` | Cathedral "
             "Membrane. \"During combat\" is part of the trigger event (CR 603.2), checked only as it dies; the "
             "creatures it blocked are read from the stack entry's triggering permanent as the ability resolves and "
             "aren't targets |")

# (anchor prefix, line): the line is inserted after the one line that starts with the anchor.
INSERTS = {
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"When this creature dies during combat, create four 1/1 green Dog creature tokens.\" |", CATHEDRAL),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("  counter on it\" in a dies / leaves-the-battlefield trigger (Chasm Skulker, The Ooze, Shadow Urchin)",
         "- `LifeLostWhenTriggeringPermanentEntered()` - \"the life you lost when it entered\": the life the resolving "
         "ability's controller lost to `LoseAllButOneLifeEffect` when the stack entry's triggering permanent entered "
         "(read from it as it last existed on the battlefield, CR 603.10a); 0 when another player lost it or nothing was "
         "recorded (Soulgorger Orgg's leaves-the-battlefield `GainLifeEffect`)"),
    ],
}

EDITS = {
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Whenever this enchantment or another nonland permanent you control is put into a graveyard from the "
         "battlefield, each opponent sacrifices a permanent of their choice that shares a card type with it.\" | "
         "`Triggers.dies(THIS)` + `Triggers.dies(PermanentNotPredicate(PermanentIsLandPredicate()), "
         "ObjectRelation.ANOTHER_YOU_CONTROL)`: `SacrificePermanentsSharingDyingPermanentTypeEffect()`", None),
        ("| \"When this creature enters, you lose all but 1 life. When this creature leaves the battlefield, you gain "
         "life equal to the life you lost when it entered.\" |",
         "| \"When this creature enters, you lose all but 1 life. When this creature leaves the battlefield, you gain "
         "life equal to the life you lost when it entered.\" | `LoseAllButOneLifeEffect()` on "
         "`Triggers.enters(ObjectRelation.THIS)` + `GainLifeEffect(new LifeLostWhenTriggeringPermanentEntered())` on "
         "`Triggers.leavesBattlefield(THIS)` | ETB + leaves-the-battlefield trigger | Soulgorger Orgg; the enters "
         "ability records the life lost on the permanent, and the amount reads it from the stack entry's triggering "
         "permanent (as it last existed on the battlefield, CR 603.10a); 0 if another player lost it |"),
        ("| \"Modular N\" |",
         "| \"Modular N\" | `STATIC EnterWithCountersEffect(PLUS_ONE_PLUS_ONE, Fixed(N))` + `Triggers.dies(THIS) "
         "new MayEffect(" + PUT_COUNTER + ".modularAbility(), " + PUT_COUNTER + ".MODULAR_PROMPT)` | keyword + death "
         "trigger | The entering replacement supplies the modular counters; the optional death trigger (CR 702.43a) "
         "counts only the +1/+1 counters on the stack entry's triggering permanent (the dying creature as it last "
         "existed on the battlefield, CR 603.10a), targets an artifact creature and is marked modular (Arcbound "
         "Bruiser). A granted \"modular 1\" is the same ability in a `GrantTriggeredAbilityEffect` (Blaster, Combat "
         "DJ) |"),
        ("| \"If a modular triggered ability would put one or more +1/+1 counters on a creature you control, that many "
         "plus one +1/+1 counters are put on it instead\" |",
         "| \"If a modular triggered ability would put one or more +1/+1 counters on a creature you control, that many "
         "plus one +1/+1 counters are put on it instead\" | `STATIC AddOnePlusOneCountersToModularAbilityEffect()` + "
         "the modular-marked `" + PUT_COUNTER + ".modularAbility()` | STATIC replacement | Zabaz, the Glimmerwasp. "
         "The `modular` flag is written by `modularAbility()` (granted modular included) and carried through the "
         "death-trigger counter placement, so the replacement does not affect ordinary +1/+1 counter events or "
         "modular creatures entering with counters |"),
        ("| \"When this creature dies, you **may** put a -1/-1 counter on target creature for each -1/-1 counter on "
         "it\" |",
         "| \"When this creature dies, you **may** put a -1/-1 counter on target creature for each -1/-1 counter on "
         "it\" | `new MayEffect(new " + PUT_COUNTER + "(CounterType.MINUS_ONE_MINUS_ONE), \"…?\")` | "
         "`Triggers.dies(THIS)` | Soulstinger: the target is still chosen as the trigger stacks (any creature, CR "
         "603.3d), and the counters may be declined at resolution (CR 603.5). Pairs with an "
         "`Triggers.enters(ObjectRelation.THIS)` `target(new ControlledPermanentPredicateTargetFilter(new "
         "PermanentIsCreaturePredicate(), …)).addEffect(…, PutCounterOnTargetPermanentEffect(MINUS_ONE_MINUS_ONE, "
         "2))` for \"put two -1/-1 counters on target creature you control\" — the ETB's \"you control\" cast-time "
         "filter does **not** leak into this death trigger (the death trigger targets any creature) |"),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("| `SacrificePermanentsSharingDyingPermanentTypeEffect` |", None),
        ("| `GainLifeEqualToLifeLostWhenEnteredEffect` |", None),
        ("| `DealDamageToBlockedAttackersOnDeathEffect` |",
         "| `DealDamageToBlockedAttackersOnDeathEffect` | `(int damage)` | \"it deals N damage to each creature it "
         "blocked this combat\" (Cathedral Membrane): the creatures the stack entry's triggering permanent (the dying "
         "creature as it last existed on the battlefield, CR 603.10a) blocked this combat, read as it resolves; not "
         "targets. \"When this creature dies during combat\" is `Triggers.dies(THIS)` with "
         "`TriggerOptions.DEFAULT.withEventCondition(new DuringCombat())` |"),
        ("| `" + PUT_COUNTER + "` |",
         "| `" + PUT_COUNTER + "` | `(CounterType counterType, PermanentPredicate targetPredicate, boolean modular)` — "
         "cards use `(CounterType)`, `(CounterType, PermanentPredicate)` or `modularAbility()` | `Triggers.dies(THIS)` "
         "only: \"when this dies, put a `counterType` counter on target creature for each `counterType` counter on "
         "it.\" The count is read from the stack entry's triggering permanent (as it last existed on the battlefield, "
         "CR 603.10a) as the effect resolves; `targetPredicate` optionally narrows the structural target (for "
         "example, to artifact creatures). Resolution places that many counters of that type on the chosen "
         "creature. \"You may\" is a `MayEffect` around it on the card (Soulstinger; modular is `new "
         "MayEffect(modularAbility(), MODULAR_PROMPT)`): the target is still chosen as the trigger stacks (CR "
         "603.3d) and the choice made at resolution (CR 603.5). `modular` marks modular's ability (CR 702.43a) for "
         "Zabaz's replacement. Always triggers (no intervening-if). Used by Grief Tyrant (mandatory), Soulstinger "
         "and the modular cards. **Death-trigger targeting note:** "
         "`TriggeredAbilityQueueService.processNextDeathTriggerTarget` only applies the card-level `target(...)` "
         "filter when THIS trigger's effect is bound to that target group (via `getEffectTargetIndex`); a cast-time "
         "filter belonging to a different ability (e.g. Soulstinger's ETB \"creature you control\") does not narrow "
         "the death trigger |"),
    ],
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("- `SacrificePermanentsSharingDyingPermanentTypeEffect()` —", None),
        ("- `GainLifeEqualToLifeLostWhenEnteredEffect()` -",
         "- `GainLifeEffect(new LifeLostWhenTriggeringPermanentEntered())` - Triggers.leavesBattlefield(THIS): the "
         "controller gains the life it lost to `LoseAllButOneLifeEffect()` when the triggering permanent entered "
         "(Soulgorger Orgg)."),
        ("- `" + PUT_COUNTER + "(CounterType counterType[, boolean optional[, PermanentPredicate targetPredicate[, "
         "boolean modular]]])` —",
         "- `" + PUT_COUNTER + "(CounterType counterType[, PermanentPredicate targetPredicate])` / `"
         + PUT_COUNTER + ".modularAbility()` — `Triggers.dies(THIS)` / `Triggers.creatureDies(YOU_CONTROL)`: \"put a "
         "`counterType` counter on target creature for each `counterType` counter on it.\" Counts the stack entry's "
         "triggering permanent's counters (as it last existed on the battlefield, CR 603.10a) and puts that many on a "
         "targeted creature (any creature, or `targetPredicate`). \"You may\" is a `MayEffect` around it on the card "
         "(Soulstinger; Reyhan's commander-zone trigger, whose \"if it had one or more +1/+1 counters on it\" is the "
         "filter: `Triggers.putIntoCommandZone(new PermanentHasAtLeastCountersPredicate(PLUS_ONE_PLUS_ONE, 1), "
         "YOU_OWN)`); the target is still chosen as the trigger stacks. Modular is `new MayEffect(modularAbility(), "
         "MODULAR_PROMPT)`: an artifact-creature target and the `modular` flag Zabaz reads. Default mandatory (Grief "
         "Tyrant). MINUS_ONE_MINUS_ONE"),
        ("- `" + PUT_COUNTER + "(CounterType counterType[, boolean optional])` —", None),
    ],
    "agent-docs/CARD_PATTERNS_CREATURES_ETB.md": [
        ("| ETB counters on own creature + \"you may\" death hand-out | `s/Soulstinger.java` |",
         "| ETB counters on own creature + \"you may\" death hand-out | `s/Soulstinger.java` | ETB "
         "target(ControlledPermanentPredicateTargetFilter(creature)).addTrigger(Triggers.enters(ObjectRelation.THIS), "
         "PutCounterOnTargetPermanentEffect(MINUS_ONE_MINUS_ONE, 2)); Triggers.dies(THIS) MayEffect("
         + PUT_COUNTER + "(MINUS_ONE_MINUS_ONE), prompt). Two differently-filtered targeted abilities on one card: "
         "the ETB's cast-time \"you control\" filter does not leak into the any-creature death trigger (per-effect "
         "filter attribution, see EFFECTS_INDEX.md) |"),
    ],
}


def main():
    for name, edits in EDITS.items():
        path = pathlib.Path(name)
        lines = path.read_text().split("\n")
        for prefix, replacement in edits:
            matches = [i for i, line in enumerate(lines) if line.startswith(prefix)]
            if replacement is not None and replacement in lines:
                continue
            if replacement is None and not matches:
                continue
            if len(matches) != 1:
                raise ValueError("%s: %d lines start with %r" % (name, len(matches), prefix[:80]))
            if replacement is None:
                del lines[matches[0]]
            else:
                lines[matches[0]] = replacement
        for anchor, line in INSERTS.get(name, []):
            if line in lines:
                continue
            matches = [i for i, existing in enumerate(lines) if existing.startswith(anchor)]
            if len(matches) != 1:
                raise ValueError("%s: %d lines start with %r" % (name, len(matches), anchor[:80]))
            lines.insert(matches[0] + 1, line)
        path.write_text("\n".join(lines))
        print("updated", name)


if __name__ == "__main__":
    main()
