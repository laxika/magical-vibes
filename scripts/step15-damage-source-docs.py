#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 8: the agent-docs for "that source" and "that source's controller".

Idempotent; run from the repository root:

    python3 scripts/step15-damage-source-docs.py

Each replacement swaps one whole line found by its unique prefix; a line already in its new form is skipped. Lines
that are removed are replaced by nothing.
"""
import pathlib
import sys

DOCS = pathlib.Path("agent-docs")

SOURCE_CONTROLLER = ("the controller of the stack entry's triggering permanent (the damage source) as the ability "
                     "resolves, or its last known controller (CR 608.2h), or the controller the entry recorded for a "
                     "source that was never a permanent (`GameQueryService.findTriggeringPermanentController`)")

EDITS = {
    "EFFECTS_INDEX.md": [
        ("| `DamageSourceControllerGainsControlOfDamagedPermanentEffect` |",
         "| `DamageSourceControllerGainsControlOfDamagedPermanentEffect` | `()` / `(UUID newControllerId)` | that "
         "source's controller gains permanent control of the damaged creature (Crag Saurian): " + SOURCE_CONTROLLER
         + ". `newControllerId` is only for a collector that names another player (Contested Game Ball's attacking "
         "player); cards use `()` |"),
        ("| `DamageSourceControllerGetsPoisonCounterEffect` |", None),
        ("| `DamageSourceControllerMillsEffect` |", None),
        ("| `DealDamageToDamageSourceCreatureOrSpellControllerEffect` |",
         "| `DealDamageToDamageSourceCreatureOrSpellControllerEffect` | `()` | Tephraderm's `Triggers.dealtDamage()` "
         "reflection, with `TriggerOptions.EACH_SOURCE`: the damage dealt (the entry's event value) to the triggering "
         "permanent if it is still a creature, or to the recorded controller of a source that was never a permanent "
         "(a spell). No target |"),
        ("| `DamageSourceControllerLosesLifeEffect` |", None),
        ("`DamageSourceController*` damage-trigger markers implement `DamageSourceControllerAwareEffect`",
         "\"That source\" of a `Triggers.dealtDamage()` or `Triggers.enchantedCreatureDealsDamage()` ability is the "
         "stack entry's triggering permanent, and \"that source's controller\" its controller as the ability resolves "
         "or its last known one (CR 608.2h); the collectors give the entry the source, its controller as it dealt the "
         "damage and the damage (event value), never binding them into the effect. Write \"that source's controller "
         "[does X] that many\" with an existing effect and a `TRIGGERING_PERMANENT_CONTROLLER` recipient "
         "(`MillRecipient`, `SacrificeRecipient`, `PoisonRecipient`, `LoseLifeRecipient`, `DamageRecipient`) and "
         "`EventValue`, and \"whenever a source deals damage to this\" with `TriggerOptions.EACH_SOURCE` (CR 603.2c)."),
        ("| `DamageSourceControllerSacrificesPermanentsEffect` |", None),
        ("| `DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect` |",
         "| `DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect` | `()` | that source's controller "
         "sacrifices that many permanents of their choice or loses that much life (Archfiend of Spite): "
         + SOURCE_CONTROLLER + "; \"that many\" is the entry's event value. Its collector judges \"a source an "
         "opponent controls\" by the source's controller as it dealt the damage |"),
        ("| `LoseLifeRecipient.EACH_OTHER_PLAYER` |",
         "| `LoseLifeRecipient.EACH_OTHER_PLAYER` | enum value | `LoseLifeEffect` scope that skips the source card's "
         "owner and makes every other player lose life in `orderedPlayerIds` order. |\n"
         "| `LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER` | enum value | `LoseLifeEffect` recipient: "
         + SOURCE_CONTROLLER.replace("(the damage source) ", "") + ". Visions of Brutality: `LoseLifeEffect(new "
         "EventValue(), TRIGGERING_PERMANENT_CONTROLLER)` on `Triggers.enchantedCreatureDealsDamage()`. |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `DamageSourceControllerMillsEffect()` —",
         "- \"Whenever a source deals damage to this creature, that source's controller [mills / sacrifices / gets a "
         "poison counter / ...] that many\": `addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE, ...)` "
         "with an existing effect, an `EventValue` amount and a `TRIGGERING_PERMANENT_CONTROLLER` recipient — "
         "`MillEffect(new EventValue(), MillRecipient.TRIGGERING_PERMANENT_CONTROLLER)` (Belltower Sphinx), "
         "`SacrificePermanentsEffect(new EventValue(), new PermanentTruePredicate(), "
         "SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER)` (Phyrexian Obliterator), `GivePoisonCountersEffect(1, "
         "PoisonRecipient.TRIGGERING_PERMANENT_CONTROLLER)` (Reaper of Sheoldred). The entry's triggering permanent "
         "is the source; the recipient is its controller as the ability resolves, or its last known controller "
         "(CR 608.2h)"),
        ("- `DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect()` —",
         "- `DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect()` — on `Triggers.dealtDamage()` with "
         "`TriggerOptions.EACH_SOURCE`: that source's controller (as the ability resolves) sacrifices that many "
         "permanents of their choice or loses that much life (Archfiend of Spite); reads the entry's triggering "
         "permanent and event value"),
        ("- `DealDamageToDamageSourceCreatureOrSpellControllerEffect()` —",
         "- `DealDamageToDamageSourceCreatureOrSpellControllerEffect()` — on `Triggers.dealtDamage()` with "
         "`TriggerOptions.EACH_SOURCE`: \"this deals that much damage to that creature\" / \"to that spell's "
         "controller\" (Tephraderm); reads the entry's triggering permanent (the creature) or its recorded controller "
         "(a spell) and event value. Implements `DamageDealingEffect`"),
        ("- `DamageSourceControllerLosesLifeEffect()` —",
         "- \"Its controller loses that much life\" of an enchanted creature that dealt damage: `LoseLifeEffect(new "
         "EventValue(), LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER)` on "
         "`Triggers.enchantedCreatureDealsDamage()` (Visions of Brutality)"),
        ("- `DamageSourceControllerSacrificesPermanentsEffect(", None),
        ("`DamageSourceControllerGainsControlOfDamagedPermanentEffect()` —",
         "`DamageSourceControllerGainsControlOfDamagedPermanentEffect()` — on `Triggers.dealtDamage()` with "
         "`TriggerOptions.EACH_SOURCE`: that source's controller (as the ability resolves, CR 608.2h) gains "
         "permanent control of this creature (Crag Saurian)."),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Whenever a source deals damage to this creature, that source's controller gains control of this creature\" |",
         "| \"Whenever a source deals damage to this creature, that source's controller gains control of this "
         "creature\" | `DamageSourceControllerGainsControlOfDamagedPermanentEffect()` | `Triggers.dealtDamage()`, "
         "`TriggerOptions.EACH_SOURCE` | Crag Saurian. The source's controller as the ability resolves gains control "
         "(CR 608.2h); the entry's triggering permanent is the source |"),
        ("| \"Whenever a source deals damage to this creature, that source's controller mills that many cards\" |",
         "| \"Whenever a source deals damage to this creature, that source's controller mills that many cards\" | "
         "`MillEffect(new EventValue(), MillRecipient.TRIGGERING_PERMANENT_CONTROLLER)` | `Triggers.dealtDamage()`, "
         "`TriggerOptions.EACH_SOURCE` | Belltower Sphinx. Once per damage source (CR 603.2c); the source's "
         "controller as the ability resolves mills, or its last known controller (CR 608.2h) |"),
        ("| \"Whenever a source an opponent controls deals damage to this creature, that source's controller loses that much life unless",
         "| \"Whenever a source an opponent controls deals damage to this creature, that source's controller loses "
         "that much life unless they sacrifice that many permanents of their choice\" | "
         "`DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect()` | `Triggers.dealtDamage()`, "
         "`TriggerOptions.EACH_SOURCE` | Archfiend of Spite. \"An opponent controls\" is judged as the damage is "
         "dealt; the player who chooses is the source's controller as the ability resolves (official ruling, "
         "CR 608.2h), and the life-loss branch applies when that many legal permanents are unavailable |"),
        ("| \"whenever a creature deals damage to this creature, this creature deals that much damage to that creature\" /",
         "| \"whenever a creature deals damage to this creature, this creature deals that much damage to that "
         "creature\" / \"whenever a spell deals damage to this creature, this creature deals that much damage to that "
         "spell's controller\" | `DealDamageToDamageSourceCreatureOrSpellControllerEffect()` | "
         "`Triggers.dealtDamage()`, `TriggerOptions.EACH_SOURCE` | Tephraderm. The entry's triggering permanent is "
         "the creature, or a spell's recorded controller is dealt the damage; no target |"),
        ("| \"Whenever this creature is dealt combat damage, you gain that much life and attacking player loses that much life\" |",
         "| \"Whenever this creature is dealt combat damage, you gain that much life and attacking player loses that "
         "much life\" | `SequenceEffect.of(GainLifeEffect(new EventValue()), LoseLifeEffect(new EventValue(), "
         "LoseLifeRecipient.ACTIVE_PLAYER))` | `Triggers.dealtCombatDamage()` | Souls of the Faultless. The attacking "
         "player is the active player (CR 506.2); the loss is life loss, not damage |"),
        ("| \"Whenever a permanent becomes untapped, [that permanent's controller does effect]\" |",
         "| \"Whenever a permanent becomes untapped, [that permanent's controller does effect]\" | `[effect using the "
         "untapped permanent's controller]` | `Triggers.permanentBecomesUntapped()` | Fires once for every permanent "
         "on every battlefield that transitions tapped→untapped. The trigger records the untapped permanent and its "
         "controller; Mesmeric Orb uses `MillEffect(1, MillRecipient.TRIGGERING_PERMANENT_CONTROLLER)`: the "
         "permanent's controller as the ability resolves, or the recorded controller once it has left |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| On dealt damage: reflect damage to a creature source or its spell controller |",
         "| On dealt damage: reflect damage to a creature source or its spell controller | `t/Tephraderm.java` | "
         "`Triggers.dealtDamage()`, `TriggerOptions.EACH_SOURCE`, `DealDamageToDamageSourceCreatureOrSpellControllerEffect()` "
         "— reads the entry's triggering permanent (the creature) or the spell's recorded controller and the damage "
         "dealt; no target |"),
        ("| On dealt damage: source controller poison |",
         "| On dealt damage: source controller poison | `r/ReaperOfSheoldred.java` | `Triggers.dealtDamage()`, "
         "`TriggerOptions.EACH_SOURCE`, `GivePoisonCountersEffect(1, PoisonRecipient.TRIGGERING_PERMANENT_CONTROLLER)` "
         "— the source's controller as the ability resolves |"),
        ("| On dealt damage: source controller mills that many |",
         "| On dealt damage: source controller mills that many | `b/BelltowerSphinx.java` | `Triggers.dealtDamage()`, "
         "`TriggerOptions.EACH_SOURCE`, `MillEffect(new EventValue(), MillRecipient.TRIGGERING_PERMANENT_CONTROLLER)` |"),
        ("| Combat damage received: gain life and source controller loses that much life |",
         "| Combat damage received: gain life and attacking player loses that much life | `s/SoulsOfTheFaultless.java` "
         "| `Triggers.dealtCombatDamage()` `SequenceEffect.of(GainLifeEffect(new EventValue()), LoseLifeEffect(new "
         "EventValue(), LoseLifeRecipient.ACTIVE_PLAYER))` — one ability, the damage dealt as its event value |"),
        ("| On dealt damage: source controller sacrifices |",
         "| On dealt damage: source controller sacrifices | `p/PhyrexianObliterator.java` | `Triggers.dealtDamage()`, "
         "`TriggerOptions.EACH_SOURCE`, `SacrificePermanentsEffect(new EventValue(), new PermanentTruePredicate(), "
         "SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER)` — the source's controller as the ability resolves "
         "chooses that many permanents |"),
    ],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("| Global untap mill trigger |",
         "| Global untap mill trigger | `m/MesmericOrb.java` | Triggers.permanentBecomesUntapped() `MillEffect(1, "
         "MillRecipient.TRIGGERING_PERMANENT_CONTROLLER)` — each permanent that becomes untapped causes its controller "
         "to mill one; the trigger records that controller for a permanent that leaves before resolution |"),
    ],
}


def apply(path, edits):
    lines = path.read_text().split("\n")
    changed = False
    for prefix, replacement in edits:
        matches = [index for index, line in enumerate(lines) if line.startswith(prefix)]
        if replacement is not None and all(line in lines for line in replacement.split("\n")):
            continue
        if not matches:
            if replacement is None:
                continue
            sys.exit(f"{path}: no line starts with {prefix!r}")
        if len(matches) > 1:
            sys.exit(f"{path}: {len(matches)} lines start with {prefix!r}")
        index = matches[0]
        if replacement is None:
            del lines[index]
            if index < len(lines) and lines[index] == "" and index > 0 and lines[index - 1] == "":
                del lines[index]
        else:
            lines[index:index + 1] = replacement.split("\n")
        changed = True
    if changed:
        path.write_text("\n".join(lines))
        print(f"updated {path}")


def main():
    for name, edits in EDITS.items():
        apply(DOCS / name, edits)


if __name__ == "__main__":
    main()
