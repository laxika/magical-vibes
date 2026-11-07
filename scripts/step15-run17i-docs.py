#!/usr/bin/env python3
"""Step 15, run 17i: agent-docs and the implement-card skill for an attack trigger's event conditions.

A condition that is part of the trigger event ("whenever this and at least two other creatures attack", "attacks while
saddled", "attacks a battle") is `withEventCondition`; only an "if" that immediately follows the trigger condition is
`withInterveningIf` (CR 603.4). Exact row replacements; idempotent; fails if an old row is neither present nor already
replaced, or if a removed form is still named.
"""
import pathlib
import re
import sys

RULE = ("A condition that is part of the trigger event — \"whenever this and at least two other creatures attack\", "
        "\"whenever you attack with three or more creatures\", \"attacks while saddled\", \"attacks a battle / an "
        "opponent / the monarch\", \"whenever all non-Wall creatures you control attack\" — is "
        "`TriggerOptions.DEFAULT.withEventCondition(condition)`, judged only as the ability triggers; "
        "`withInterveningIf` is only for an \"if\" that immediately follows the trigger condition (CR 603.4), checked "
        "again as the ability resolves. So is a condition on which the ability exists (a Class's level, a solved Case, "
        "a Siege's mode, an unlocked door).")

EDITS = {
    "agent-docs/EFFECTS_INDEX.md": [
        ("| `ConditionalEffect(new MinimumAttackers(minimumAttackers), wrapped)` | see construction shape | "
         "Trigger-condition wrapper:",
         "| `TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(minimumAttackers))` | see construction "
         "shape | The trigger event's attacker count (an event condition, not an intervening \"if\": CR 603.4); was a "
         "trigger-condition wrapper:"),
        ("`SourceIsSaddled` (the source permanent is saddled until end of turn; the attack trigger condition of "
         "\"whenever this creature attacks while saddled\" is filtered at declaration time in `CombatAttackService`; "
         "Brightfield Glider),",
         "`SourceIsSaddled` (the source permanent is saddled until end of turn; \"whenever this creature attacks while "
         "saddled\" is `Triggers.attacks()` with `withEventCondition(new SourceIsSaddled())`, judged as the attackers "
         "are declared; Brightfield Glider),"),
    ],
    "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("`Triggers.attacks()` + `ConditionalEffect(MinimumAttackers(4), ChooseBlockersThisCombatEffect())` — Odric + 3 "
         "others;",
         "`Triggers.attacks()` + `TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(4))`, "
         "`ChooseBlockersThisCombatEffect()` — Odric + 3 others;"),
        ("`Triggers.attacks()` + `ConditionalEffect(MinimumAttackers(3), SequenceEffect.of(GrantKeywordEffect("
         "INDESTRUCTIBLE, OWN_CREATURES), GrantKeywordEffect(INDESTRUCTIBLE, SELF)))`",
         "`Triggers.attacks()` + `TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(3))` (battalion is the "
         "trigger event), `GrantKeywordEffect(INDESTRUCTIBLE, OWN_CREATURES), GrantKeywordEffect(INDESTRUCTIBLE, SELF)`"),
        ("`Triggers.attacks()` + `ConditionalEffect(AttackingCreaturesTotalPowerAtLeast(6), CreateTokenEffect(..., "
         "tappedAndAttacking=true))` — effective power is checked when attackers are declared and the conditional is "
         "unwrapped as the trigger is created",
         "`Triggers.attacks()` + `TriggerOptions.DEFAULT.withInterveningIf(new AttackingCreaturesTotalPowerAtLeast(6))`, "
         "`CreateTokenEffect(..., tappedAndAttacking=true)` — an intervening \"if\" about the attack as declared "
         "(`DeclaredAttackCondition`): judged from the declared attackers, and its wrapper comes off once judged"),
    ],
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `Triggers.attacks() ConditionalEffect(SourceIsSaddled, SeekLibraryEffect(CardTypePredicate(LAND), "
         "Integer.MAX_VALUE, true))`",
         "| `Triggers.attacks()`, `withEventCondition(new SourceIsSaddled())`, `SeekLibraryEffect(CardTypePredicate(LAND), "
         "Integer.MAX_VALUE, true)`"),
        ("| `ConditionalEffect(new SourceIsSaddled(), SequenceEffect.of(BoostSelfEffect(1, 2), GrantKeywordEffect(FLYING, "
         "SELF)))` | Triggers.attacks() | Brightfield Glider. The attack condition is checked when the creature is "
         "declared as an attacker; no trigger is created if it was not saddled then |",
         "| `TriggerOptions.DEFAULT.withEventCondition(new SourceIsSaddled())`, `BoostSelfEffect(1, 2)`, "
         "`GrantKeywordEffect(FLYING, SELF)` | Triggers.attacks() | Brightfield Glider. \"While saddled\" is part of the "
         "trigger event: checked when the creature is declared as an attacker, not again as the ability resolves |"),
        ("| `ConditionalEffect(MinimumAttackers(2), LookAtTopCardsEffect(",
         "| `withEventCondition(new MinimumAttackers(2))`, `LookAtTopCardsEffect("),
        ("| `ConditionalEffect(new MinimumAttackers(4), new ChooseBlockersThisCombatEffect())` | `Triggers.attacks()` | "
         "Odric, Master Tactician. Triggers.attacks() ensures CARDNAME is attacking; MinimumAttackers(4) = self + 3 "
         "others. Filtered at trigger collection; attacker count snapshotted via xValue so removal in response still "
         "resolves |",
         "| `TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(4))`, `new ChooseBlockersThisCombatEffect()` "
         "| `Triggers.attacks()` | Odric, Master Tactician. Triggers.attacks() ensures CARDNAME is attacking; "
         "MinimumAttackers(4) = self + 3 others, the trigger event's attacker count (an event condition), so removal in "
         "response still resolves |"),
        ("| `ConditionalEffect(AttackingCreaturesTotalPowerAtLeast(6), MayEffect(new "
         "SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(new PermanentIsCreaturePredicate()), \"Sacrifice "
         "another creature?\"))` | `Triggers.attacks()` | Tiger-Tribe Hunter. The pack-tactics condition is checked when "
         "the attack trigger is created;",
         "| `TriggerOptions.DEFAULT.withInterveningIf(new AttackingCreaturesTotalPowerAtLeast(6))`, `MayEffect(new "
         "SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(new PermanentIsCreaturePredicate()), \"Sacrifice "
         "another creature?\")` | `Triggers.attacks()` | Tiger-Tribe Hunter. The pack-tactics intervening \"if\" is "
         "judged from the declared attackers (`DeclaredAttackCondition`);"),
        ("`target(anyTarget()).addTrigger(Triggers.attacks(), ConditionalEffect(SourceIsSaddled, "
         "DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectsIfCreatureDamagedEffect(1, List.of(CantBlockEffect, "
         "DamageNotRemovedDuringCleanupEffect)))` + `SaddleCost(1)` | Triggers.attacks() + activated ability | "
         "Switchgrass Grazer (YOTJ 14) |",
         "`target(anyTarget()).addTrigger(Triggers.attacks(), TriggerOptions.DEFAULT.withInterveningIf(new "
         "SourceIsSaddled()), DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectsIfCreatureDamagedEffect(1, "
         "List.of(CantBlockEffect, DamageNotRemovedDuringCleanupEffect)))` + `SaddleCost(1)` | Triggers.attacks() + "
         "activated ability | Switchgrass Grazer (YOTJ 14). Known rules bug: this gates the whole ability on "
         "\"saddled\", where the Oracle text gates only the perpetual grant (effect-slot-refactor.md, step 15 run 17i's "
         "Findings) |"),
    ],
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("(Melee; Odric, Master Tactician via `Triggers.attacks()` + `MinimumAttackers(4)`)",
         "(Melee; Odric, Master Tactician via `Triggers.attacks()` + `withEventCondition(new MinimumAttackers(4))`)"),
        ("- `SourceIsSaddled` — source permanent is saddled until end of turn; use as the attack-time condition for "
         "\"whenever this creature attacks while saddled\" (Brightfield Glider)",
         "- `SourceIsSaddled` — source permanent is saddled until end of turn; \"whenever this creature attacks while "
         "saddled\" is `withEventCondition(new SourceIsSaddled())` on `Triggers.attacks()` (Brightfield Glider)\n"
         "- `DeclaredAttackCondition` — marker of a condition about the attack as declared (`AttackingCreaturesTotalPower"
         "AtLeast`, `ExactlyAttackers`): as a \"whenever this creature attacks\" ability's intervening \"if\" (\"if you "
         "attacked with … this combat\", pack tactics, Creepy Puppeteer) it is judged from the declared attackers and "
         "not recounted as the ability resolves"),
    ],
    "agent-docs/CARD_IMPLEMENTATION_PLAYBOOK.md": [
        ("never as a `ConditionalEffect` around the effects.",
         "never as a `ConditionalEffect` around the effects. " + RULE),
    ],
    ".claude/skills/implement-card/SKILL.md": [],
}

SKILL_ANCHOR = "**Triggers on this object's own zone change**"
SKILL_LINE = "**A condition of the trigger event is an event condition.** " + RULE + "\n\n"

REMOVED = [
    re.compile(r"ConditionalEffect\(\s*(new )?MinimumAttackers\("),
    re.compile(r"ConditionalEffect\(\s*(new )?SourceIsSaddled"),
    re.compile(r"ConditionalEffect\(AttackingCreaturesTotalPowerAtLeast"),
]


def main():
    for name, edits in EDITS.items():
        path = pathlib.Path(name)
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: expected one occurrence of {old[:100]!r}")
            text = text.replace(old, new)
        if name.endswith("SKILL.md") and SKILL_LINE not in text:
            if text.count(SKILL_ANCHOR) != 1:
                raise SystemExit("implement-card skill: anchor missing")
            text = text.replace(SKILL_ANCHOR, SKILL_LINE + SKILL_ANCHOR)
        path.write_text(text)
    offenders = []
    for path in list(pathlib.Path("agent-docs").rglob("*.md")) + list(pathlib.Path(".claude/skills").rglob("*.md")):
        for number, line in enumerate(path.read_text().splitlines(), 1):
            if "Triggers.attacks()" in line and any(pattern.search(line) for pattern in REMOVED):
                offenders.append(f"{path}:{number}")
    if offenders:
        raise SystemExit("removed self-attack forms still named:\n" + "\n".join(offenders))
    print("docs updated")


if __name__ == "__main__":
    sys.exit(main())
