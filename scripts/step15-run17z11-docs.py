#!/usr/bin/env python3
"""Step 15, run 17z11: the agent-docs rows of the forms run 17z11 changed.

- `SetBasePowerToughnessOfOtherAttackingCreatureEffect` reads the other attacker from the entry's attacking creatures;
  `OtherAttackingCreatureReferenceEffect` is gone.
- `DealDamageToTargetPlayerOrPlaneswalkerEffect.byTriggeringCreature` is the "that creature deals" of any trigger whose
  event names a creature (Rage Forger's attacker, Stalking Vengeance's dying creature).
- `SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect` reads the triggering player.
- Forbidden Orchard's "target opponent" is `CreateTokenForTargetPlayerEffect(…, PlayerRelation.OPPONENT)`, chosen as
  the ability goes on the stack; Treasure Nabber's "an opponent" is any opponent.

Idempotent; fails if a doc still names a removed form outside a "Gone" line.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("the combat trigger collector fixes the other attacker when the trigger is created (Creepy Puppeteer)",
         "\"that creature\" is the one other creature declared as an attacker with the source, read as the ability "
         "resolves from the stack entry's attacking creatures, which the attack collector records (Creepy Puppeteer)"),
        ("`DealDamageToTargetPlayerOrPlaneswalkerEffect.byTriggeringCreature(DynamicAmount)` makes the creature that "
         "caused a death trigger the damage source (its lifelink applies; Stalking Vengeance).",
         "`DealDamageToTargetPlayerOrPlaneswalkerEffect.byTriggeringCreature(DynamicAmount)` makes the creature the "
         "trigger event was about, the stack entry's triggering permanent as it is or last existed, the damage source "
         "(its lifelink and colors apply; Stalking Vengeance's dying creature, Rage Forger's \"have that creature "
         "deal\")."),
    ],
    "EFFECTS_INDEX.md": [
        ("`byTriggeringCreature(DynamicAmount)` makes the creature that died the damage source (Stalking Vengeance) |",
         "`byTriggeringCreature(DynamicAmount)` makes the creature the trigger event was about, the stack entry's "
         "triggering permanent, the damage source (Stalking Vengeance's dying creature, Rage Forger's attacker) |"),
        ("StackEntry context: targetId = damaged player, sourcePermanentId = source creature.",
         "\"That player\" is the stack entry's triggering player, the player dealt damage; \"it\" the source "
         "permanent."),
        ("The combat trigger collector fixes the other attacker as event context, and resolution reuses "
         "`SetBasePowerToughnessEffect` (Creepy Puppeteer)",
         "\"That creature\" is read as the ability resolves from the stack entry's attacking creatures (the one "
         "declared with the source), and resolution reuses `SetBasePowerToughnessEffect` (Creepy Puppeteer)"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("new MayEffect(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1), \"…\")` | `Triggers.creatureYouControlAttacks()` | "
         "Rage Forger. The permanent-conditional filters by the attacking permanent's counters; the `MayEffect` becomes a "
         "CR 603.5 resolution-time may whose source permanent is the attacker (\"that creature\").",
         "new MayEffect(DealDamageToTargetPlayerOrPlaneswalkerEffect.byTriggeringCreature(new Fixed(1)), \"…\")` | "
         "`Triggers.creatureYouControlAttacks()` | Rage Forger. The permanent-conditional filters by the attacking "
         "permanent's counters; the `MayEffect` becomes a CR 603.5 resolution-time may, and \"that creature\", the "
         "attacking creature the event was about, deals the damage."),
        ("The mana-tap collector filters the event to an opponent's artifact and binds the tapped permanent as a "
         "non-targeting trigger reference |",
         "The mana-tap collector filters the event to an artifact any opponent tapped and binds the tapped permanent "
         "as a non-targeting trigger reference |"),
        ("`CreateTokenForTargetPlayerEffect(CreateTokenEffect(\"Spirit\", 1, 1, null, [SPIRIT], {}, {}))` | "
         "Triggers.playerTapsLandForMana() | Forbidden Orchard. The collector gates on the source land being the tapped "
         "land and on its controller doing the tapping, then gives the token to the opponent.",
         "`CreateTokenForTargetPlayerEffect(CreateTokenEffect(\"Spirit\", 1, 1, null, [SPIRIT], {}, {}), "
         "PlayerRelation.OPPONENT)` | Triggers.playerTapsLandForMana() | Forbidden Orchard. The collector gates on the "
         "source land being the tapped land and on its controller doing the tapping; the targeted ability is no mana "
         "ability, and its target opponent is chosen as it goes on the stack."),
    ],
}

REMOVED = ["OtherAttackingCreatureReferenceEffect"]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if old not in text:
                raise SystemExit(f"{name}: the expected text is missing: {old[:90]}")
            text = text.replace(old, new)
        path.write_text(text)
        print(f"{name}: up to date")
    for path in [*DOCS.rglob("*.md"), *(ROOT / ".claude/skills").rglob("*.md")]:
        for line in path.read_text().split("\n"):
            if any(form in line for form in REMOVED) and "Gone" not in line:
                sys.exit(f"{path}: still names a removed form: {line[:100]}")


if __name__ == "__main__":
    main()
