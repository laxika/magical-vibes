#!/usr/bin/env python3
"""Step 15, run 17v: agent-docs rows of the forms the run changed or deleted.

Each edit replaces the whole line that starts with a key (a line the run's forms appear on) with its new text, or
drops it when the new text is None (a mojibake duplicate of a rewritten row); the "#feedback" group replaces text
inside a line. Idempotent: a line already rewritten no longer starts with its key. Fails if a
doc or skill still names a deleted class.

Usage: step15-run17v-docs.py (from the repository root)
"""
import pathlib
import sys

DELETED = [
    "EnchantedCreatureControllerLosesLifeEffect",
    "EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect",
    "EnchantedCreatureDiesGainLifeAndDrawEqualToToughnessEffect",
    "EnchantedControllerSacrificesCreatureOnLeaveEffect",
    "PutCountersOnSourceEqualToDyingPowerEffect",
]

LKI = "the creature as it last existed on the battlefield, CR 603.10a"

EDITS = {
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("- `EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect()`", None),
        ("- `ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect()`",
         "- `ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect()` — Triggers.dies(ObjectRelation.ENCHANTED): return the "
         "source Aura attached to a legal creature chosen by the dying creature's controller (the stack entry's "
         "triggering permanent's, read as it resolves); if none exists, return it as a non-Aura enchantment and persist "
         "`DealDamageToPlayerAtUpkeepEffect` for that player (Takklemaggot)"),
        ("- `PutCountersOnSourceEqualToDyingPowerEffect(", None),
        ("- `EnchantedControllerSacrificesCreatureOnLeaveEffect()`", None),
        ("- `ReturnSourceAuraToChosenCreatureOnLeaveEffect()`",
         "- `ReturnSourceAuraToChosenCreatureOnLeaveEffect()` — returns this Aura from its owner's graveyard when the "
         "enchanted permanent leaves the battlefield; the leaving permanent's controller (the stack entry's triggering "
         "permanent's, as it last existed) chooses a legal creature to enchant and gets the Aura. Use in "
         "`Triggers.leavesBattlefield(null, ObjectRelation.ENCHANTED)`. \"Its controller sacrifices a creature\" (Funeral "
         "March) is `SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), "
         "SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER)`."),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("| `EnchantedCreatureDiesGainLifeAndDrawEqualToToughnessEffect` |", None),
        ("| `EnchantedControllerSacrificesCreatureOnLeaveEffect` |", None),
        ("| `ReturnSourceAuraToChosenCreatureOnLeaveEffect` |",
         "| `ReturnSourceAuraToChosenCreatureOnLeaveEffect` | `()` | when the enchanted permanent leaves the "
         "battlefield, return this Aura from its owner's graveyard to the battlefield and let the leaving permanent's "
         "controller (the stack entry's triggering permanent's, as it last existed) choose a legal creature to enchant. "
         "Use with `Triggers.leavesBattlefield(null, ObjectRelation.ENCHANTED)` |"),
        ("| `ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect` |",
         "| `ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect` | `()` | when enchanted creature dies, return this Aura "
         "attached to a legal creature chosen by the dying creature's controller (the stack entry's triggering "
         "permanent's, read as it resolves); if none exists, return it under its controller's control as a non-Aura "
         "enchantment with a persistent fixed-player upkeep damage trigger. Use with "
         "`Triggers.dies(ObjectRelation.ENCHANTED)` (Takklemaggot) |"),
        ("| `EnchantedCreatureControllerLosesLifeEffect` |", None),
        ("| `EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect` |", None),
        ("| `PutCountersOnSourceEqualToDyingPowerEffect` |", None),
        ("| `ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect` |",
         "| `ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect` | `(DynamicAmount count)` | Dead Man's Chest: "
         "exiles `count` cards (`new TriggeringPermanentPower()`, \"its power\") from the top of the library of the "
         "stack entry's triggering permanent's owner and lets the controller cast the nonland ones while they stay "
         "exiled, with mana of any type |"),
    ],
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"When enchanted creature leaves the battlefield, its controller sacrifices a creature of their choice\" |",
         "| \"When enchanted creature leaves the battlefield, its controller sacrifices a creature of their choice\" | "
         "`SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), "
         "SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER)` | `Triggers.leavesBattlefield(null, "
         "ObjectRelation.ENCHANTED)` | Funeral March (Aura). \"Its controller\" is the leaving creature's controller "
         "(the stack entry's triggering permanent's, as it last existed), not the Aura's |"),
        ("| \"When enchanted creature leaves the battlefield, that creature's controller returns this Aura",
         "| \"When enchanted creature leaves the battlefield, that creature's controller returns this Aura from its "
         "owner's graveyard to the battlefield\" | `ReturnSourceAuraToChosenCreatureOnLeaveEffect()` | "
         "`Triggers.leavesBattlefield(null, ObjectRelation.ENCHANTED)` | Traveling Plague. The leaving creature's "
         "controller (the stack entry's triggering permanent's, as it last existed) chooses a legal creature for the "
         "Aura to enchant and the Aura returns under that player's control with no counters |"),
        ("| \"Whenever another creature dies, you may put X +1/+1 counters on this creature, where X is that "
         "creature's power\" |",
         "| \"Whenever another creature dies, you may put X +1/+1 counters on this creature, where X is that "
         "creature's power\" | `MayEffect(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new "
         "TriggeringPermanentPower()), prompt)` | `Triggers.creatureDies(ANOTHER)` | Kresh the Bloodbraided. \"That "
         "creature's power\" is the dying creature's last-known power the stack entry records; without \"you may\" "
         "the bare `PutCountersOnSelfEffect` (Vincent Valentine, `creatureDies(OPPONENT_CONTROLS)`) |"),
        ("| \"Enchanted creature has \\\"Cumulative upkeep-Pay 1 life.\\\" When enchanted creature dies, its "
         "controller loses 2 life\" |",
         "| \"Enchanted creature has \\\"Cumulative upkeep-Pay 1 life.\\\" When enchanted creature dies, its "
         "controller loses 2 life\" | `target(PermanentAllOf(IsCreature, ColorIn(BLACK)))` + STATIC "
         "`GrantEffectEffect(CumulativeUpkeepEffect.life(1), GrantScope.ENCHANTED_CREATURE)` + "
         "Triggers.dies(ObjectRelation.ENCHANTED) `LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER)` "
         "| STATIC + trigger | Decomposition. `GrantedUpkeepEffectSupport` feeds the granted CU into the enchanted "
         "creature's upkeep; \"its controller\" is the dying creature's (the stack entry's triggering permanent) |"),
        ("| \"When enchanted creature dies, that creature's controller loses life equal to its toughness\" |",
         "| \"When enchanted creature dies, that creature's controller loses life equal to its toughness\" | "
         "`LoseLifeEffect(new TriggeringPermanentToughness(), LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER)` | "
         "Triggers.dies(ObjectRelation.ENCHANTED) | Banewasp Affliction. The enchanted-death collector records the "
         "dying creature as the stack entry's triggering permanent (" + LKI + "): its controller, power and "
         "toughness. \"X is its power\" is `TriggeringPermanentPower` (or the entry's event value) |"),
        ("| \"When enchanted creature dies, its controller loses life equal to its power and you gain life equal to "
         "its toughness\" |",
         "| \"When enchanted creature dies, its controller loses life equal to its power and you gain life equal to "
         "its toughness\" | `SequenceEffect.of(new LoseLifeEffect(new TriggeringPermanentPower(), "
         "LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER), new GainLifeEffect(new TriggeringPermanentToughness()))` | "
         "Triggers.dies(ObjectRelation.ENCHANTED) | Death Watch. One ability (loss then gain); \"you gain X life and "
         "draw X cards, where X is its toughness\" is the same `SequenceEffect` of `GainLifeEffect` and "
         "`DrawCardEffect` (Fruit of the First Tree) |"),
        ("| \"When enchanted creature dies, this Aura deals damage equal to that creature's toughness to the "
         "creature's controller\" |",
         "| \"When enchanted creature dies, this Aura deals damage equal to that creature's toughness to the "
         "creature's controller\" | `DealDamageToPlayersEffect(new TriggeringPermanentToughness(), "
         "DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)` | Triggers.dies(ObjectRelation.ENCHANTED) | Creature Bond. "
         "Damage (not life loss), the creature as it last existed. Contrast the life-loss sibling Banewasp Affliction |"),
    ],
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md#feedback": [
        ("Contrast `EnchantedCreatureControllerLosesLifeEffect` (Numbing Dose) which loses life |",
         "Contrast `LoseLifeEffect(N, LoseLifeRecipient.TRIGGERING_PLAYER)` (Numbing Dose) which loses life |"),
    ],
    "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| Another creature dies (may put X +1/+1 counters = its power) |",
         "| Another creature dies (may put X +1/+1 counters = its power) | `k/KreshTheBloodbraided.java` | "
         "Triggers.creatureDies(ANOTHER) MayEffect(PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE, new "
         "TriggeringPermanentPower())) — X = the dying creature's last-known power, read from the stack entry |"),
    ],
    "agent-docs/CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("| Enchant black creature: granted life-CU + fixed death drain |",
         "| Enchant black creature: granted life-CU + fixed death drain | `d/Decomposition.java` | "
         "target(PermanentAllOf(IsCreature, ColorIn(BLACK))) + STATIC GrantEffectEffect(CumulativeUpkeepEffect.life(1), "
         "ENCHANTED_CREATURE) + Triggers.dies(ObjectRelation.ENCHANTED) LoseLifeEffect(2, "
         "TRIGGERING_PERMANENT_CONTROLLER) — the enchanted creature's own controller pays the CU "
         "(GrantedUpkeepEffectSupport); declining sacrifices it, which then triggers the 2-life drain |"),
        ("| Enchant creature: death damages its controller = toughness |",
         "| Enchant creature: death damages its controller = toughness | `c/CreatureBond.java` | "
         "target(PermanentIsCreaturePredicate) + Triggers.dies(ObjectRelation.ENCHANTED) DealDamageToPlayersEffect(new "
         "TriggeringPermanentToughness(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER) — damage (not life loss) "
         "equal to the dying creature's last-known toughness to its controller, both read from the stack entry's "
         "triggering permanent. Damage sibling of Banewasp Affliction (life loss) |"),
        ("| Enchant creature: death drains power / gains toughness |",
         "| Enchant creature: death drains power / gains toughness | `d/DeathWatch.java` | target(creature) + "
         "Triggers.dies(ObjectRelation.ENCHANTED) SequenceEffect.of(LoseLifeEffect(new TriggeringPermanentPower(), "
         "TRIGGERING_PERMANENT_CONTROLLER), GainLifeEffect(new TriggeringPermanentToughness())) — one ability |"),
        ("| Enchanted creature LTB → its controller sacrifices |",
         "| Enchanted creature LTB → its controller sacrifices | `f/FuneralMarch.java` | target creature + "
         "Triggers.leavesBattlefield(null, ObjectRelation.ENCHANTED) SacrificePermanentsEffect(1, new "
         "PermanentIsCreaturePredicate(), SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER) — the leaving creature's "
         "controller (the stack entry's triggering permanent's, not the Aura's) sacrifices a creature of their choice |"),
        ("| Enchanted creature LTB â†’ its controller sacrifices |", None),
    ],
}


def main():
    for name, edits in EDITS.items():
        path = pathlib.Path(name.split("#")[0])
        text = path.read_text(encoding="utf-8", errors="surrogateescape")
        if name.endswith("#feedback"):
            for old, new in edits:
                text_new = text.replace(old, new)
                if text_new != text:
                    path.write_text(text_new, encoding="utf-8", errors="surrogateescape")
                    print(f"updated {path}")
            continue
        lines = text.split("\n")
        out = []
        for line in lines:
            for key, new in edits:
                if line.startswith(key) and line != new:
                    line = new
                    break
            if line is not None:
                out.append(line)
        new_text = "\n".join(out)
        if new_text != text:
            path.write_text(new_text, encoding="utf-8", errors="surrogateescape")
            print(f"updated {path}")
    stale = []
    for path in list(pathlib.Path("agent-docs").rglob("*.md")) + list(pathlib.Path(".claude/skills").rglob("*.md")):
        if path.name == "EFFECT_MERGE_PLAN.md":
            continue
        text = path.read_text(encoding="utf-8", errors="surrogateescape")
        for cls in DELETED:
            if cls in text:
                stale.append(f"{path}: {cls}")
    if stale:
        raise SystemExit("docs still name deleted classes:\n" + "\n".join(stale))


if __name__ == "__main__":
    sys.exit(main())
