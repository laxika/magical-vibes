#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 13b: the agent-docs say the granting permanent is the stack entry's.

Idempotent: python3 scripts/step15-granting-permanent-docs.py

GrantingPermanentAwareEffect is gone. A granted triggered ability names the permanent that granted it
(TriggeredAbility.grantingPermanentId: the Aura or Equipment of an attachment grant, the source of a one-shot grant),
the collector that puts it on the stack copies it to StackEntry.grantingPermanentId, and the effects that name that
permanent read it there. Stensian Sanguinist's "this creature becomes prepared" is
BecomePreparedEffect.ofGrantingPermanent().
"""
import pathlib

DOCS = pathlib.Path("agent-docs")

EDITS = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("""  source permanent, excluding the bound granting attachment (Animal Friend).""",
         """  source permanent, other than the permanent that granted the ability (the stack entry's granting permanent,
  `StackEntry.getGrantingPermanentId()`: Animal Friend's "other than Animal Friend")."""),
        ("""`.excludingGrantingPermanent()` excludes the specific permanent granting the ability, while permitting other copies with the same name (Dire Blunderbuss). The granting source is bound by `GrantingPermanentAwareEffect`""",
         """`.excludingGrantingPermanent()` excludes the specific permanent granting the ability, while permitting other copies with the same name (Dire Blunderbuss), and `.sacrificingGrantingPermanent()` allows only that permanent (Sunfire Torch). The granting permanent is the stack entry's (`StackEntry.getGrantingPermanentId()`), which the collector copies from the granted ability"""),
        ("""- `BecomePreparedEffect()` — source becomes "prepared" (Strixhaven);""",
         """- `BecomePreparedEffect()` / `BecomePreparedEffect.ofGrantingPermanent()` — source (or, in an ability this permanent grants another, this permanent: Stensian Sanguinist) becomes "prepared" (Strixhaven);"""),
        ("""The trigger collector binds the granting Equipment through `GrantingPermanentAwareEffect`; the wrapper has no target""",
         """The Equipment is the stack entry's granting permanent (`StackEntry.getGrantingPermanentId()`), which the collector copies from the granted ability; the wrapper has no target"""),
        ("""- `TransformGrantingPermanentEffect()` — toggles the face of the permanent that granted this temporary effect;""",
         """- `TransformGrantingPermanentEffect()` — toggles the face of the permanent that granted this temporary ability (the stack entry's granting permanent);"""),
        ("""A nested `GrantingPermanentAwareEffect` is bound to the original source permanent""",
         """The granted ability names the original source permanent as its granting permanent (`TriggeredAbility.grantingPermanentId()`)"""),
        ("""Stores in combat-triggered storage and binds nested `GrantingPermanentAwareEffect`s to the granting permanent.""",
         """Stores in combat-triggered storage, the ability naming the resolving ability's source as its granting permanent (`TriggeredAbility.grantingPermanentId()`)."""),
    ],
    "EFFECTS_INDEX.md": [
        ("""| `SacrificeGrantingEquipmentThenEffect` | `(CardEffect thenEffect[, UUID grantingEquipmentId])` | Sacrifices the Equipment that granted the resolving ability and creates a reflexive triggered ability with `thenEffect` only if that sacrifice succeeds. The trigger collector binds the granting Equipment through `GrantingPermanentAwareEffect`;""",
         """| `SacrificeGrantingEquipmentThenEffect` | `(CardEffect thenEffect)` | Sacrifices the Equipment that granted the resolving ability and creates a reflexive triggered ability with `thenEffect` only if that sacrifice succeeds. The Equipment is the stack entry's granting permanent (`StackEntry.getGrantingPermanentId()`), which the collector copies from the granted ability;"""),
        ("""excluding the bound granting attachment. Used by Animal Friend (PIP 72) |""",
         """other than the stack entry's granting permanent ("other than Animal Friend"). Used by Animal Friend (PIP 72) |"""),
        ("""| `TransformGrantingPermanentEffect` | `()` / `(UUID grantingPermanentId)` | transforms the permanent that granted this temporary effect; the no-arg form is bound when granted through `GrantEffectToChosenCreatureUntilEndOfTurnEffect` |""",
         """| `TransformGrantingPermanentEffect` | `()` | transforms the permanent that granted this temporary ability, the stack entry's granting permanent; `GrantEffectToChosenCreatureUntilEndOfTurnEffect` names its source as the ability's granting permanent |"""),
        ("""Nested `GrantingPermanentAwareEffect`s are bound to the granting permanent before storage.""",
         """The stored ability names the resolving ability's source as its granting permanent (`TriggeredAbility.grantingPermanentId()`)."""),
        ("""| `BecomePreparedEffect` | `()` | "Prepared" (Secrets of Strixhaven): the source permanent becomes prepared.""",
         """| `BecomePreparedEffect` | `()` / `ofGrantingPermanent()` | "Prepared" (Secrets of Strixhaven): the source permanent (`ofGrantingPermanent()`: the stack entry's granting permanent, in an ability this permanent grants another — Stensian Sanguinist) becomes prepared."""),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("""The granted trigger binds the specific Animal Friend permanent so it is excluded even when other Auras or Equipment are attached.""",
         """The granted ability names the Animal Friend that grants it (its granting permanent), so that one is excluded even when other Auras or Equipment are attached."""),
        ("""GrantEffectToTargetUntilEndOfCombatEffect(Triggers.dealsCombatDamageToPlayer(), new BecomePreparedEffect()))` | Triggers.attacks() | Stensian Sanguinist // Exsanguinate (SOC 29). The combat-duration grant binds `BecomePreparedEffect` to the attacking Sanguinist because the later combat-damage trigger's source is the targeted creature |""",
         """GrantEffectToTargetUntilEndOfCombatEffect(Triggers.dealsCombatDamageToPlayer(), BecomePreparedEffect.ofGrantingPermanent()))` | Triggers.attacks() | Stensian Sanguinist // Exsanguinate (SOC 29). The combat-duration grant names the Sanguinist as the granted ability's granting permanent, which `ofGrantingPermanent()` prepares, because the later combat-damage trigger's source is the targeted creature |"""),
    ],
}


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        updated = text
        for old, new in edits:
            if new in updated:
                continue
            if updated.count(old) != 1:
                raise SystemExit(f"{path}: expected one {old!r}, found {updated.count(old)}")
            updated = updated.replace(old, new)
        if updated != text:
            path.write_text(updated)
            print(f"updated {path}")


if __name__ == "__main__":
    main()
