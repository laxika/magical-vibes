#!/usr/bin/env python3
"""Step 15, run 17y4: the agent-docs lines that name the removed records or the changed card forms.

Whole-line edits keyed by the line's start (the old line is replaced, or the new line inserted after an anchor line),
so a second run changes nothing. Fails if a doc or skill still names a removed record outside a "Gone" line.

Usage: step15-run17y4-docs.py
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
RUN = "effect-slot-refactor.md step 15, run 17y4"
SOWER = ("`Triggers.sourceDealsDamageToPermanentOrPlayer().withDamagedPlayerFilter(new PlayerChosenBySourcePredicate())` "
         "+ `LoseLifeEffect(new EventValue(), LoseLifeRecipient.OTHER_CHOSEN_PLAYER)`")
GHYRSON = ("`Triggers.anotherSourceYouControlDealsDamageToPermanentOrPlayer()` with "
           "`TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new EventValueAtLeast(2)))` + "
           "`DealDamageToAnyTargetEffect(2)`")
LOLTH = ("`CreateEmblemEffect(new TriggeredAbility(Triggers.creaturesYouControlDealCombatDamageToOpponent(), "
         "TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new TargetPlayerLostLifeThisTurn(8))), "
         "new LoseLifeEffect(new Sum(new Fixed(8), new Scaled(new LifeLostThisTurn(CountScope.TARGET_PLAYER), -1)), "
         "LoseLifeRecipient.TRIGGERING_PLAYER)), \"…\")`")
MAARIKA = ("`SacrificePermanentsEffect(1, noncreature nonland, SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER)` on "
           "`Triggers.dealsDamageToCreature()` with `TriggerOptions.DEFAULT.withInterveningIf(new "
           "TriggeringPermanentMatches(new PermanentDealtExcessDamageThisTurnPredicate()))`")

# (file, line start, replacement line, "replace" | "after")
EDITS = [
    ("agent-docs/EFFECTS_INDEX.md", "| `OtherChosenPlayerLosesLifeEffect` |",
     f"| `OtherChosenPlayerLosesLifeEffect` | — | **Gone** ({RUN}): Sower of Discord writes {SOWER} |", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `GhyrsonStarnKelermorphEffect` |",
     f"| `GhyrsonStarnKelermorphEffect` | — | **Gone** ({RUN}): Ghyrson Starn writes {GHYRSON}; the recipient is the "
     "entry's non-targeting `targetId` |", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `EmblemCombatDamageTriggerEffect` |",
     f"| `EmblemCombatDamageTriggerEffect` | — | **Gone** ({RUN}): an emblem's \"whenever an opponent is dealt combat "
     "damage by one or more creatures you control\" is a `TriggeredAbility` on "
     "`Triggers.creaturesYouControlDealCombatDamageToOpponent()`, read by `checkEmblemCombatDamageTriggers` |",
     "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `LoseLifeToThresholdOnCombatDamageEffect` |",
     f"| `LoseLifeToThresholdOnCombatDamageEffect` | — | **Gone** ({RUN}): Lolth, Spider Queen writes {LOLTH} |",
     "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "- `ChoosePlayerOnEnterEffect(2)` uses the multi-player as-enters choice path",
     "- `ChoosePlayerOnEnterEffect(2)` uses the multi-player as-enters choice path and stores both selected players on "
     "the entering permanent (`Permanent.rememberedTargetPlayerIds`), which `PlayerChosenBySourcePredicate` and "
     "`LoseLifeRecipient.OTHER_CHOSEN_PLAYER` read (Sower of Discord).", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `SacrificeNoncreatureNonlandPermanentIfExcessDamageEffect` |",
     "| `SacrificeNoncreatureNonlandPermanentIfExcessDamageEffect` | — | **Gone** (effect-slot-refactor.md step 15, "
     f"run 17y; no card wrote it): Maarika writes {MAARIKA} |", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `LoseLifeRecipient.EACH_OTHER_PLAYER` |",
     "| `LoseLifeRecipient.OTHER_CHOSEN_PLAYER` | enum value | `LoseLifeEffect` scope for \"the other chosen player\": "
     "each player the source chose as it entered (`Permanent.rememberedTargetPlayerIds`, read from the source or its "
     "last-known information) other than the stack entry's triggering player. Sower of Discord |", "after"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `OtherChosenPlayerLosesLifeEffect()` - global",
     f"- \"Whenever damage is dealt to one of the chosen players, the other chosen player also loses that much life\" — "
     f"{SOWER}: once for each source and chosen player, that player the entry's triggering player (Sower of Discord).",
     "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `GhyrsonStarnKelermorphEffect()` — global watcher",
     f"- \"Whenever another source you control deals exactly 1 damage to a permanent or player, this deals 2 damage to "
     f"that permanent or player\" — {GHYRSON}: once for each source and recipient, the recipient the entry's "
     "non-targeting `targetId` (Ghyrson Starn, Kelermorph)", "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `OtherChosenPlayerLosesLifeEffect()` — global watcher",
     f"- `OtherChosenPlayerLosesLifeEffect` — **Gone** ({RUN}): Sower of Discord writes {SOWER}", "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `LoseLifeToThresholdOnCombatDamageEffect(int lifeThreshold)`",
     f"- `LoseLifeToThresholdOnCombatDamageEffect` — **Gone** ({RUN}): Lolth, Spider Queen's emblem is {LOLTH}; the "
     "emblem collector gives the entry the damaged opponent as its non-targeting `targetId` and triggering player",
     "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "`SacrificeNoncreatureNonlandPermanentIfExcessDamageEffect` — **Gone**",
     "`SacrificeNoncreatureNonlandPermanentIfExcessDamageEffect` — **Gone** (effect-slot-refactor.md step 15, run 17y; "
     f"no card wrote it): Maarika, Brutal Gladiator writes {MAARIKA}", "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `LoseLifeRecipient.EACH_OTHER_PLAYER` —",
     "- `LoseLifeRecipient.OTHER_CHOSEN_PLAYER` — `LoseLifeEffect` recipient for \"the other chosen player\": the "
     "players the source chose as it entered but the entry's triggering player (Sower of Discord)", "after"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", "| \"As this creature enters, choose two players. Whenever damage is dealt",
     "| \"As this creature enters, choose two players. Whenever damage is dealt to one of the chosen players, the other "
     "chosen player also loses that much life.\" | `STATIC ChoosePlayerOnEnterEffect(2)` + "
     "`LoseLifeEffect(new EventValue(), LoseLifeRecipient.OTHER_CHOSEN_PLAYER)` | STATIC + "
     "`Triggers.sourceDealsDamageToPermanentOrPlayer().withDamagedPlayerFilter(new PlayerChosenBySourcePredicate())` | "
     "Sower of Discord (CMM 187). The as-enters choice remembers both players; the trigger fires once for each source "
     "and chosen player dealt damage, that player the entry's triggering player and the damage its event value. |",
     "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever another source you control deals exactly 1 damage",
     "| \"Whenever another source you control deals exactly 1 damage to a permanent or player, Ghyrson Starn deals 2 "
     "damage to that permanent or player\" | `TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new "
     "EventValueAtLeast(2)))` + `DealDamageToAnyTargetEffect(2)` | "
     "`Triggers.anotherSourceYouControlDealsDamageToPermanentOrPlayer()` | Ghyrson Starn, Kelermorph (`40K` 124) — once "
     "for each source and recipient (its ruling); the recipient is the entry's non-targeting `targetId` |", "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", "| \"You get an emblem with \\\"Whenever an opponent is dealt combat damage",
     "| \"You get an emblem with \\\"Whenever an opponent is dealt combat damage by one or more creatures you control, "
     "if that player lost less than 8 life this turn, they lose life equal to the difference.\\\"\" | "
     f"{LOLTH} | loyalty ability | Lolth, Spider Queen −8; `TriggerCollectionService.checkEmblemCombatDamageTriggers` "
     "puts the emblem's ability on the stack once for each opponent dealt combat damage, that player its non-targeting "
     "`targetId`; the intervening \"if\" is checked as it triggers and as it resolves |", "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever this creature deals damage to a creature, if that creature was "
     "dealt excess damage this turn",
     "| \"Whenever this creature deals damage to a creature, if that creature was dealt excess damage this turn, that "
     "creature's controller sacrifices a noncreature, nonland permanent.\" | `SacrificePermanentsEffect(1, noncreature "
     "nonland, SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER)` with `TriggerOptions.DEFAULT.withInterveningIf(new "
     "TriggeringPermanentMatches(new PermanentDealtExcessDamageThisTurnPredicate()))` | `Triggers.dealsDamageToCreature()` "
     "| Maarika, Brutal Gladiator. The damaged creature is the entry's triggering permanent; the intervening \"if\" is "
     "judged with it as the ability triggers and again as it resolves. |", "replace"),
    ("agent-docs/CARD_PATTERN_INDEX.md", "| exact-1-damage source trigger reflected to each matching permanent or player |",
     "| exact-1-damage source trigger reflected to each matching permanent or player | `g/GhyrsonStarnKelermorph.java` "
     "(`Triggers.anotherSourceYouControlDealsDamageToPermanentOrPlayer()` + event condition) |", "replace"),
    ("agent-docs/PREDICATES_REFERENCE.md", "| `PermanentDealtDamageThisTurnPredicate` |",
     "| `PermanentDealtExcessDamageThisTurnPredicate` | `()` | permanents dealt excess damage this turn (CR 120.10; "
     "evaluated against `GameData.permanentsDealtExcessDamageThisTurn`). Maarika's \"if that creature was dealt excess "
     "damage this turn\" is `TriggeringPermanentMatches(new PermanentDealtExcessDamageThisTurnPredicate())` | `gameData` |",
     "after"),
    ("agent-docs/PREDICATES_REFERENCE.md", "| `PlayerOtherThanSourceOwnerPredicate` |",
     "| `PlayerChosenBySourcePredicate` | `()` | a player the source permanent chose as it entered "
     "(`Permanent.rememberedTargetPlayerIds`, `ChoosePlayerOnEnterEffect(2)`); source-relative. Sower of Discord's "
     "\"one of the chosen players\" as a damage trigger's `withDamagedPlayerFilter` |", "after"),
    ("agent-docs/ACTIVATED_ABILITY_GUIDE.md", "| `Triggers.sourceDealsDamageToYou()` |",
     "| `Triggers.sourceDealsDamageToPermanentOrPlayer()` / `anotherSourceYouControlDealsDamageToPermanentOrPlayer()` | "
     "\"Whenever [another source you control] deals damage to a permanent or player\" / \"whenever damage is dealt to "
     "…\" — once for each source and each permanent and player it deals damage to (Ghyrson Starn's ruling): the "
     "recipient is the entry's non-targeting `targetId` (a player also its triggering player, a permanent its triggering "
     "permanent), the damage dealt to it the event value. `withFilter` / `withSpellFilter` narrow the source, "
     "`withDamagedFilter` / `withDamagedPlayerFilter` the recipient (Sower of Discord: `PlayerChosenBySourcePredicate`); "
     "\"exactly 1 damage\" is an event condition on the event value |", "after"),
    ("agent-docs/ACTIVATED_ABILITY_GUIDE.md", "| `Triggers.goadedCreaturesDealCombatDamageToOpponent()` |",
     "| `Triggers.creaturesYouControlDealCombatDamageToOpponent()` | \"Whenever an opponent is dealt combat damage by "
     "one or more creatures you control\" on an **emblem** (Lolth, Spider Queen): once for each opponent in a combat "
     "damage step, that player the entry's non-targeting `targetId` and triggering player, the combat damage dealt to "
     "them the event value. Only the emblem collector reads it |", "after"),
]

GONE = ["OtherChosenPlayerLosesLifeEffect", "GhyrsonStarnKelermorphEffect", "EmblemCombatDamageTriggerEffect",
        "LoseLifeToThresholdOnCombatDamageEffect"]


def main():
    for file, start, line, mode in EDITS:
        path = ROOT / file
        lines = path.read_text().split("\n")
        if line in lines:
            continue
        hits = [i for i, l in enumerate(lines) if l.startswith(start)]
        if mode == "replace":
            if len(hits) != 1:
                raise SystemExit(f"{file}: {len(hits)} lines start with {start!r}")
            lines[hits[0]] = line
        else:
            if len(hits) != 1:
                raise SystemExit(f"{file}: {len(hits)} anchor lines start with {start!r}")
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
