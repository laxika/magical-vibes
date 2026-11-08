#!/usr/bin/env python3
"""Step 15, run 17y5: the agent-docs lines that name the removed records and marker or the changed card forms.

Whole-line edits keyed by the line's start (the old line is replaced, the new line inserted after an anchor line, or
a sentence appended to the line), so a second run changes nothing. Fails if a doc or skill still names a removed
record outside a "Gone" line.

Usage: step15-run17y5-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RUN = "effect-slot-refactor.md step 15, run 17y5"
DEALT_BY = "`.dealtByEventDamageSource()`"
EACH_OTHER = "`DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OTHER_OPPONENT)`"
SASKIA = ("`DealDamageToPlayersEffect(new EventValue(), DamageRecipient.CHOSEN_PLAYER).dealtByEventDamageSource()`")
GREATBOW = ("`DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)"
            ".dealtByEventDamageSource()`")
EVENT_SOURCE = ("the permanent that dealt the damage the trigger event was about (the entry's event damage source, "
                "`StackEntry.getEventDamageSource()`, which the \"deals damage to a creature\" and \"deals combat "
                "damage to a player\" collectors record) deals the damage instead of the ability's source, as it is "
                "or as it last existed (CR 608.2h): its lifelink, infect and colors for protection apply")

# (file, line start, replacement line, "replace" | "after" | "append")
EDITS = [
    ("agent-docs/EFFECTS_INDEX.md", "| `DealDamageToChosenPlayerEffect` |",
     f"| `DealDamageToChosenPlayerEffect` | — | **Gone** ({RUN}): Saskia the Unyielding writes {SASKIA} |", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `DealDamageToEachOtherOpponentEffect` |",
     f"| `DealDamageToEachOtherOpponentEffect` | — | **Gone** ({RUN}): \"it deals that much damage to each other "
     f"opponent\" is {EACH_OTHER} (Hydra Omnivore, Amarant Coral, Kosei, Vincent: \"it\" is the ability's source), "
     f"with {DEALT_BY} when \"it\" is the creature that dealt the combat damage (Kediss); Parapet Thrasher's \"this "
     "creature deals 4 damage to each other opponent\" is `DealDamageToPlayersEffect(4, EACH_OTHER_OPPONENT)` |",
     "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `ReflectAllyDamageToDamagedCreatureControllerEffect` |",
     f"| `ReflectAllyDamageToDamagedCreatureControllerEffect` | — | **Gone** ({RUN}): Greatbow Doyen writes {GREATBOW} "
     "on `Triggers.creatureYouControlDealsDamageToCreature().withFilter(…)`. Flayed Nim's former self combat "
     "life-loss form is `LoseLifeEffect(new EventValue(), TRIGGERING_PERMANENT_CONTROLLER)` on "
     "`Triggers.dealsDamageToCreature().withCombatDamageOnly()` |", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `DealDamageToPlayersEffect` + `DamageRecipient.CHOSEN_PLAYER` |",
     "| `DealDamageToPlayersEffect` + `DamageRecipient.CHOSEN_PLAYER` | `(DynamicAmount, CHOSEN_PLAYER)` | "
     "non-targeting combat-damage follow-up that deals the triggering creature's combat damage again to the source "
     "permanent's remembered as-enters player; `CombatDamageTriggerContextEffect.CHOSEN_PLAYER` carries that player "
     f"in `targetId`. Saskia the Unyielding (SLD 55) adds {DEALT_BY}: the creature that dealt the combat damage "
     "deals it |", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `DealDamageToPlayersEffect` + `DamageRecipient.CHOSEN_PLAYER` |",
     f"| `DealDamageToPlayersEffect.dealtByEventDamageSource()` | `DamageSource.EVENT_DAMAGE_SOURCE` | {EVENT_SOURCE}. "
     "\"That Archer deals that much damage to that creature's controller\" (Greatbow Doyen), \"it deals that much "
     f"damage to the chosen player / each other opponent\" of a creature you control (Saskia, Kediss) ({RUN}) |",
     "after"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `DealDamageToPlayersEffect(new EventValue(), DamageRecipient.CHOSEN_PLAYER)` —",
     "- `DealDamageToPlayersEffect(new EventValue(), DamageRecipient.CHOSEN_PLAYER)` — on an ally combat-damage "
     "trigger, deals the recorded combat damage to the source permanent's remembered as-enters player; Saskia the "
     f"Unyielding's \"it deals that much damage to the chosen player\" adds {DEALT_BY}, so the creature that dealt "
     "the combat damage deals it.", "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `DealDamageToChosenPlayerEffect(",
     f"- `DealDamageToChosenPlayerEffect` — **Gone** ({RUN}): {SASKIA}.", "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `DealDamageToEachOtherOpponentEffect(",
     f"- `DealDamageToEachOtherOpponentEffect` — **Gone** ({RUN}): {EACH_OTHER} (\"it\" the ability's source), with "
     f"{DEALT_BY} when \"it\" is the creature you control that dealt the combat damage (Kediss).", "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "`DealDamageToEachOtherOpponentEffect` — the no-arg",
     f"`DealDamageToPlayersEffect.dealtByEventDamageSource()` — {EVENT_SOURCE} (Greatbow Doyen, Saskia, Kediss); "
     "without it the ability's source deals the damage (Hydra Omnivore's \"it\", Parapet Thrasher's \"this "
     "creature\").", "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md",
     " - `DealDamageToPlayersEffect(DynamicAmount, DamageRecipient[, boolean unpreventable])`;",
     f" `.dealtByEventDamageSource()`: {EVENT_SOURCE} (\"that Archer deals that much damage\", Greatbow Doyen).",
     "append"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md",
     "| \"As CARDNAME enters, choose a player. Whenever a creature you control deals combat damage to a player, it",
     "| \"As CARDNAME enters, choose a player. Whenever a creature you control deals combat damage to a player, it "
     "deals that much damage to the chosen player.\" | `ChoosePlayerOnEnterEffect()` + "
     f"`trigger.withFilter(PermanentIsCreaturePredicate)`, {SASKIA} | STATIC + "
     "`Triggers.creatureYouControlDealsCombatDamageToPlayer()` | Saskia the Unyielding (SLD 55). The combat "
     "collector records the creature that dealt the combat damage as the entry's event damage source, which deals "
     "the damage |", "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md",
     "| \"As Saskia enters, choose a player. Whenever a creature you control deals combat damage to a player, it",
     "| \"As Saskia enters, choose a player. Whenever a creature you control deals combat damage to a player, it "
     "deals that much damage to the chosen player.\" | `STATIC ChoosePlayerOnEnterEffect` + "
     f"`trigger.withFilter(PermanentIsCreaturePredicate)`, {SASKIA} | STATIC + "
     "`Triggers.creatureYouControlDealsCombatDamageToPlayer()` | Saskia the Unyielding. Saskia is the ability's "
     "source; the creature that dealt the combat damage deals the damage, as it last existed if it has left the "
     "battlefield (CR 608.2h). |", "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever this creature deals combat damage to an opponent, it deals that much damage to each other opponent.\" |",
     "| \"Whenever this creature deals combat damage to an opponent, it deals that much damage to each other "
     f"opponent.\" | {EACH_OTHER} | `Triggers.dealsCombatDamageToPlayer()` | Hydra Omnivore (CMD 161). The combat "
     "collector records the damaged opponent as the entry's target, which `EACH_OTHER_OPPONENT` leaves out; "
     "\"Whenever a commander you control deals combat damage to an opponent, it deals …\" (Kediss) adds "
     f"{DEALT_BY} |", "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md",
     "| \"whenever a [subtype] you control deals damage to a creature, that [subtype] deals that much damage to that",
     "| \"whenever a [subtype] you control deals damage to a creature, that [subtype] deals that much damage to that "
     f"creature's controller\" | {GREATBOW} | "
     "`Triggers.creatureYouControlDealsDamageToCreature().withFilter(new PermanentHasSubtypePredicate(subtype))` | "
     "Greatbow Doyen — fires for combat and non-combat damage; the creature that dealt the damage (the entry's "
     "event damage source, not the watcher) deals the reflected damage (CR 608.2h), to the damaged creature's "
     "controller as the ability resolves. \"That creature's controller loses that much life\" of this creature's "
     "combat damage: `LoseLifeEffect(new EventValue(), TRIGGERING_PERMANENT_CONTROLLER)` on "
     "`Triggers.dealsDamageToCreature().withCombatDamageOnly()` (Flayed Nim) |", "replace"),
    ("agent-docs/ACTIVATED_ABILITY_GUIDE.md",
     "| `Triggers.creatureYouControlDealsDamageToCreature()` (and `dealsDamageToCreature()`,",
     "| `Triggers.creatureYouControlDealsDamageToCreature()` (and `dealsDamageToCreature()`, "
     "`equippedCreatureDealsDamageToCreature()`, `creatureDealsDamageToCreature()`) | A creature you control (this "
     "creature, equipped creature, any creature) deals damage — combat or non-combat — to a creature; same "
     "layer-aware creature gate on the *damaged* permanent as the two slots above. `.withFilter` narrows the creature "
     "that deals it, `.withDamagedFilter` the creature dealt it, `.withCombatDamageOnly()` the damage; the damaged "
     "creature is the stack entry's triggering permanent, the creature that dealt it its event damage source and the "
     f"damage its event value. Greatbow Doyen (`.withFilter(Archer)` + {GREATBOW}) |", "replace"),
    ("agent-docs/CARD_PATTERNS_PERMANENTS_STATIC.md", "| Subtype lord (own) + damage reflection |",
     "| Subtype lord (own) + damage reflection | `g/GreatbowDoyen.java` | STATIC StaticBoostEffect(1, 1, "
     "OWN_CREATURES, PermanentHasSubtypePredicate(ARCHER)) + "
     "Triggers.creatureYouControlDealsDamageToCreature().withFilter(PermanentHasSubtypePredicate(ARCHER)) "
     "DealDamageToPlayersEffect(new EventValue(), TRIGGERING_PERMANENT_CONTROLLER).dealtByEventDamageSource() — "
     "+1/+1 to other Archers you control, and whenever an Archer you control deals damage to a creature that Archer "
     "deals that much to that creature's controller |", "replace"),
    ("agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "| Creature combat damage → repeat that damage to a remembered player |",
     "| Creature combat damage → repeat that damage to a remembered player | `s/SaskiaTheUnyielding.java` | "
     "`ChoosePlayerOnEnterEffect()` + `Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter("
     f"PermanentIsCreaturePredicate)`, {SASKIA} — the remembered player is carried non-targetingly and the dealing "
     "creature deals the damage |", "replace"),
    ("agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "| Combat damage to an opponent -> same damage to each other opponent |",
     "| Combat damage to an opponent -> same damage to each other opponent | `h/HydraOmnivore.java` | "
     f"`Triggers.dealsCombatDamageToPlayer()` {EACH_OTHER}; the combat collector records the damaged opponent as the "
     "entry's target and the damage as its event value, and the ability deals separate noncombat damage to the "
     "remaining opponents |", "replace"),
]

GONE = ["ReflectAllyDamageToDamagedCreatureControllerEffect", "DealDamageToChosenPlayerEffect",
        "DealDamageToEachOtherOpponentEffect", "CombatDamageDealerReferencingEffect"]


def main():
    for file, start, line, mode in EDITS:
        path = ROOT / file
        lines = path.read_text().split("\n")
        if mode == "append":
            hits = [i for i, l in enumerate(lines) if l.startswith(start)]
            if len(hits) != 1:
                raise SystemExit(f"{file}: {len(hits)} lines start with {start!r}")
            if lines[hits[0]].endswith(line):
                continue
            lines[hits[0]] += line
        elif line in lines:
            continue
        else:
            hits = [i for i, l in enumerate(lines) if l.startswith(start)]
            if len(hits) != 1:
                raise SystemExit(f"{file}: {len(hits)} lines start with {start!r}")
            if mode == "replace":
                lines[hits[0]] = line
            else:
                lines.insert(hits[0] + 1, line)
        path.write_text("\n".join(lines))
        print(f"{file}: {mode} {start[:60]}")
    docs = list((ROOT / "agent-docs").glob("*.md")) + list((ROOT / ".claude").rglob("*.md"))
    for path in docs:
        for number, l in enumerate(path.read_text().split("\n"), 1):
            for gone in GONE:
                if re.search(r"\b" + gone + r"\b", l) and "**Gone**" not in l:
                    raise SystemExit(f"{path.relative_to(ROOT)}:{number} still names {gone}")


if __name__ == "__main__":
    main()
