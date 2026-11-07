#!/usr/bin/env python3
"""Step 15, run 17k: the agent docs and the implement-card skill show the "a player attacks" and "a creature attacks"
forms the run's collectors read.

- "Whenever a player attacks one of your opponents" is `Triggers.playerAttacksOneOfYourOpponents()` (once for each
  opponent attacked), "whenever an opponent attacks another one of your opponents"
  `Triggers.opponentAttacksAnotherOpponent()`; `PlayerAttacksOneOfYourOpponents` and `OpponentAttacksAnotherOpponent` are
  gone, Squall's condition is `AttackersHavePowerOrToughnessEqualToSourceChosenNumber`.
- A condition of a "whenever a player attacks" trigger event is the event condition; "whenever one or more [filter]
  attack" the trigger's filter (Killian).
- "Whenever your commander attacks" from a graveyard is `Triggers.creatureAttacks().withFilter(commander you own)`.

Exact edits: each must apply once, or have been applied (idempotent). Fails when a doc still shows a removed form.

Usage: step15-run17k-docs.py
"""
import pathlib
import re
import sys

DOCS = pathlib.Path("agent-docs")
SKILL = pathlib.Path(".claude/skills/implement-card/SKILL.md")

EDITS = {
    DOCS / "ACTIVATED_ABILITY_GUIDE.md": [
        ("""Checked in `CombatAttackService.declareAttackers`. Used by Total War and Karazikar, the Eye Tyrant |""",
         """Checked in `CombatAttackService.declareAttackers`. A condition of the trigger event ("with three or more creatures", "attacks enchanted player", "an opponent attacks") is the event condition; "one or more creatures enchanted by an Aura you control attack" the trigger's filter. Used by Total War |
| `Triggers.playerAttacksOneOfYourOpponents()` | "Whenever a player attacks one of your opponents": once for each of your opponents attacked. The stack entry's attacked target is that opponent ("that opponent", "defending player"; an intervening "if" about it reads it, Breena), its non-targeting `targetId` the attacking player ("that attacking player"). Used by Combat Calligrapher, Ellie, Squall, Gunblade Duelist |
| `Triggers.opponentAttacksAnotherOpponent()` | "Whenever an opponent attacks another one of your opponents": `playerAttacksOneOfYourOpponents()` where the attacking player is an opponent. Used by Karazikar, the Eye Tyrant |"""),
    ],
    DOCS / "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("""`Triggers.playerAttacks() ConditionalEffect(OpponentAttacksWithAtLeastCreatures(2, false), DrawCardEffect())` |""",
         """`Triggers.playerAttacks()` + `TriggerOptions.DEFAULT.withEventCondition(new OpponentAttacksWithAtLeastCreatures(2, false))`, `DrawCardEffect()` |"""),
        ("""`Triggers.playerAttacks() ConditionalEffect(AllOf(AttackingPlayerIsOpponent, MinimumAttackers(1), SourceIsTapped), MayEffect(""",
         """`Triggers.playerAttacks()` + `TriggerOptions.DEFAULT.withEventCondition(new AttackingPlayerIsOpponent()).withInterveningIf(new SourceIsTapped())`, `MayEffect("""),
        ("""attackingTargetOfTriggeringPlayer())))`; the attack target is limited""",
         """attackingTargetOfTriggeringPlayer()))`; the attack target is limited"""),
    ],
    DOCS / "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("""Triggers.playerAttacks() ConditionalEffect(AttacksEnchantedPlayer, MayEffect(CreateTokenForTriggeringPlayerEffect(tapped Zombie), ..., TARGET_PLAYER)) — fires""",
         """Triggers.playerAttacks() + event condition `AttacksEnchantedPlayer`, MayEffect(CreateTokenForTriggeringPlayerEffect(tapped Zombie), ..., TARGET_PLAYER) — fires"""),
        ("""+ Triggers.playerAttacks() `ConditionalEffect(ControllerAndEnchantedPlayerAttackEachOther, SacrificeSelfEffect)` — the""",
         """+ Triggers.playerAttacks() `TriggerOptions.DEFAULT.withEventCondition(ControllerAndEnchantedPlayerAttackEachOther), SacrificeSelfEffect` — the"""),
    ],
    DOCS / "EFFECTS_QUICK_REFERENCE.md": [
        ("""- `ConditionalEffect(new AttacksEnchantedPlayer(), CardEffect)` — in `Triggers.playerAttacks()`, true when""",
         """- `AttacksEnchantedPlayer()` — event condition of `Triggers.playerAttacks()` (`TriggerOptions.DEFAULT.withEventCondition`), true when"""),
        ("""- `ConditionalEffect(new ControllerAndEnchantedPlayerAttackEachOther(), CardEffect)` — in `Triggers.playerAttacks()`, true when""",
         """- `ControllerAndEnchantedPlayerAttackEachOther()` — event condition of `Triggers.playerAttacks()`, true when"""),
        ("""- `ConditionalEffect(new OpponentAttacksAnotherOpponent(), CardEffect)` — the opponent whose attack caused an `Triggers.playerAttacks()` trigger attacked a different opponent of the condition's controller directly (Karazikar, the Eye Tyrant)
- `ConditionalEffect(new PlayerAttacksOneOfYourOpponents(), CardEffect)` — on `Triggers.playerAttacks()`, fires once for each distinct opponent directly attacked by that combat's attacking player; the attacked opponent is captured for effects such as `CreateTokenForTriggeringPlayerEffect(token, true)` (Ellie, Brick Master)
- `ConditionalEffect(new PlayerAttacksOneOrMoreOfYourOpponents(), CardEffect)` — on `Triggers.playerAttacks()`, fires once per combat""",
         """- "Whenever a player attacks one of your opponents" is the trigger `Triggers.playerAttacksOneOfYourOpponents()`, once for each opponent attacked, that opponent the stack entry's attacked target (`CreateTokenForTriggeringPlayerEffect(token, true)`'s tokens attack it; Combat Calligrapher, Ellie, Brick Master); "whenever an opponent attacks another one of your opponents" is `Triggers.opponentAttacksAnotherOpponent()` (Karazikar, the Eye Tyrant)
- `AttackersHavePowerOrToughnessEqualToSourceChosenNumber()` — intervening "if" of `Triggers.playerAttacksOneOfYourOpponents()`: a creature attacking that opponent has power or toughness equal to the source's chosen number, checked as the ability triggers and again as it resolves (Squall, Gunblade Duelist)
- `AttackedOpponentHasMoreLifeThanAnotherOpponent()` — intervening "if" of `Triggers.playerAttacksOneOfYourOpponents()`: the attacked opponent has more life than another of your opponents (Breena, the Demagogue)
- `PlayerAttacksOneOrMoreOfYourOpponents()` — event condition of `Triggers.playerAttacks()`, fires once per combat"""),
    ],
    DOCS / "ORACLE_TEXT_EFFECT_MAP.md": [
        ("""`STATIC CanBeBlockedByAtMostNCreaturesEffect(1)` + `Triggers.playerAttacks() ConditionalEffect(AllOf(AttackingPlayerIsOpponent, MinimumAttackers(1), SourceIsTapped), MayEffect(""",
         """`STATIC CanBeBlockedByAtMostNCreaturesEffect(1)` + `Triggers.playerAttacks()` + `TriggerOptions.DEFAULT.withEventCondition(AttackingPlayerIsOpponent).withInterveningIf(SourceIsTapped)`, `MayEffect("""),
        ("""attackingTargetOfTriggeringPlayer())))` | STATIC + Triggers.playerAttacks() | Tahngarth""",
         """attackingTargetOfTriggeringPlayer()))` | STATIC + Triggers.playerAttacks() | Tahngarth"""),
        ("""PermanentIsCommanderPredicate)` + `Triggers.playerAttacks() ConditionalEffect(OpponentAttacksWithAtLeastCreatures(2, false), DrawCardEffect())` | STATIC + trigger | Everett""",
         """PermanentIsCommanderPredicate)` + `Triggers.playerAttacks()` + event condition `OpponentAttacksWithAtLeastCreatures(2, false)`, `DrawCardEffect()` | STATIC + trigger | Everett"""),
        ("""| `Triggers.playerAttacks()` + `ConditionalEffect(OpponentsAttackWithAtLeastCreatures(2), CantBlockThisCombatEffect.self())` | trigger | Flummoxed Cyclops. Any attack by an opponent counts, whichever player or planeswalker is attacked (`OpponentAttacksWithAtLeastCreatures` counts only attacks at the controller, so it would miss a multiplayer attack on another opponent). The attack-trigger collector unwraps the satisfied event condition, so the restriction still applies if attackers leave before the ability resolves. |""",
         """| `Triggers.playerAttacks()` + `TriggerOptions.DEFAULT.withEventCondition(new OpponentsAttackWithAtLeastCreatures(2))`, `CantBlockThisCombatEffect.self()` | trigger | Flummoxed Cyclops. Any attack by an opponent counts, whichever player or planeswalker is attacked (`OpponentAttacksWithAtLeastCreatures` counts only attacks at the controller, so it would miss a multiplayer attack on another opponent). An event condition is judged only as the ability triggers, so the restriction still applies if attackers leave before the ability resolves. |"""),
        ("""| `Triggers.playerAttacks()` + `ConditionalEffect(OpponentAttacksWithAtLeastCreatures(1), ExileTopCardOfAttackingPlayerLibraryAndGrantControllerPlayPermissionEffect())` |""",
         """| `Triggers.playerAttacks()` + `TriggerOptions.DEFAULT.withEventCondition(new OpponentAttacksWithAtLeastCreatures(1, false))`, `ExileTopCardOfAttackingPlayerLibraryAndGrantControllerPlayPermissionEffect()` |"""),
        ("""| `Triggers.playerAttacks()` + `ConditionalEffect(AttacksEnchantedPlayer, MayEffect(CreateTokenForTriggeringPlayerEffect(tapped Zombie), ..., TARGET_PLAYER))` |""",
         """| `Triggers.playerAttacks()` + `TriggerOptions.DEFAULT.withEventCondition(new AttacksEnchantedPlayer())`, `MayEffect(CreateTokenForTriggeringPlayerEffect(tapped Zombie), ..., TARGET_PLAYER)` |"""),
        ("""+ `Triggers.playerAttacks()` + `ConditionalEffect(ControllerAndEnchantedPlayerAttackEachOther, SacrificeSelfEffect)` | end step + attack trigger |""",
         """+ `Triggers.playerAttacks()` + `TriggerOptions.DEFAULT.withEventCondition(ControllerAndEnchantedPlayerAttackEachOther)`, `SacrificeSelfEffect` | end step + attack trigger |"""),
        ("""| `ConditionalEffect(AttacksPlayerAlone(), ChooseOneEffect(...))` | Triggers.creatureYouControlAttacks() | Jin Sakai, Ghost of Tsushima (SLD 2226) — the condition counts attackers by the player they attack, and the trigger records the attacking creature for both modes |""",
         """| `TriggerOptions.DEFAULT.withInterveningIf(AttacksPlayerAlone())`, `ChooseOneAtTriggerTimeEffect(ChooseOneEffect(...))` with each mode's effect in `ConditionalEffect(AttacksPlayerAlone(), ...)` | Triggers.creatureYouControlAttacks() | Jin Sakai, Ghost of Tsushima (SLD 2226) — the condition counts attackers by the player they attack, and the trigger records the attacking creature for both modes. The mode is chosen as the ability triggers (CR 603.3c), so the intervening "if" is written into each mode for the check as it resolves (CR 603.4); the collector takes the ability's wrapper off the modal choice |"""),
        ("""| `ConditionalEffect(new PlayerAttacksOneOfYourOpponents(), new CreateTokenForTriggeringPlayerEffect(new CreateTokenEffect(1, "Cordyceps Infected", 1, 1, BLACK, List.of(FUNGUS, ZOMBIE), true), true))` | `Triggers.playerAttacks()` | Ellie, Brick Master (SLD 2197); the combat collector queues one trigger per distinct opponent directly attacked |""",
         """| `new CreateTokenForTriggeringPlayerEffect(new CreateTokenEffect(1, "Cordyceps Infected", 1, 1, BLACK, List.of(FUNGUS, ZOMBIE), true), true)` | `Triggers.playerAttacksOneOfYourOpponents()` | Ellie, Brick Master (SLD 2197); the trigger fires once for each opponent attacked, that opponent the stack entry's attacked target the tokens attack |"""),
        ("""+ `ConditionalEffect(new HasAttacker(PermanentIsCommanderPredicate()), new MayPayManaEffect(...))` on `Triggers.youAttack() from the graveyard` | SPELL + graveyard triggers | Endless Ranks of HYDRA (MSC 43). The token amount is the number of opponents; the commander predicate is evaluated against the entering permanent and attacking permanents while the card remains in its owner's graveyard. |""",
         """+ the same `MayPayManaEffect` on `Triggers.creatureAttacks().withFilter(new PermanentIsCommanderPredicate(true))` from the graveyard | SPELL + graveyard triggers | Endless Ranks of HYDRA (MSC 43). The token amount is the number of opponents; "your commander attacks" is a per-creature trigger filtered to a commander the card's owner owns, so it triggers once for each such attacking creature, whoever controls it, while the card remains in its owner's graveyard. |"""),
    ],
    DOCS / "TRIGGER_SLOT_TARGETING.md": [
        ("""| `Triggers.playerAttacks()` | `CombatAttackService.declareAttackers` (all battlefields, any attacking player; attacking player stored as non-targeting `targetId`; `PlayerAttacksOneOfYourOpponents` additionally queues once per distinct opponent directly attacked and captures that opponent) | Non-targeting (Total War) |""",
         """| `Triggers.playerAttacks()` | `CombatAttackService.declareAttackers` (all battlefields, any attacking player; attacking player stored as non-targeting `targetId`) | Non-targeting (Total War) |
| `Triggers.playerAttacksOneOfYourOpponents()` / `Triggers.opponentAttacksAnotherOpponent()` | `CombatAttackService.checkPlayerAttacksYourOpponentTriggers` (once for each of the source controller's opponents attacked; that opponent the attacked target, the attacking player the non-targeting `targetId`) | Non-targeting (Ellie, Brick Master; Karazikar) |
| `Triggers.creatureAttacks()` from the graveyard | `CombatAttackService.checkGraveyardCreatureAttacksTriggers` (each graveyard, once for each attacking creature matching the filter, judged from the card and its owner: "whenever your commander attacks") | Non-targeting (Vivi's Persistence) |"""),
    ],
    SKILL: [
        ("""is the trigger's filter, `Triggers.youAttack().withFilter(predicate)` ("that many" is `new EventValue()`, the number of matching attackers);""",
         """is the trigger's filter, `Triggers.youAttack().withFilter(predicate)` ("that many" is `new EventValue()`, the number of matching attackers), and likewise `Triggers.playerAttacks().withFilter(predicate)` for "whenever one or more [filter] attack"; "whenever a player attacks one of your opponents" is `Triggers.playerAttacksOneOfYourOpponents()` (once for each opponent attacked), "whenever your commander attacks" `Triggers.creatureAttacks().withFilter(new PermanentIsCommanderPredicate(true))`;"""),
    ],
}

FORBIDDEN = [
    re.compile(r"PlayerAttacksOneOfYourOpponents\b(?!WithPower)"),
    re.compile(r"OpponentAttacksAnotherOpponent"),
    re.compile(r"PlayerAttacksOneOfYourOpponentsWithPowerOrToughness"),
    re.compile(r"HasAttacker\(new PermanentIsCommanderPredicate|HasAttacker\(PermanentIsCommanderPredicate"),
]


def main():
    failed = False
    for path, edits in EDITS.items():
        text = path.read_text()
        for old, new in edits:
            if text.count(old) == 1:
                text = text.replace(old, new)
            elif new not in text:
                print(f"{path}: edit not found: {old[:90]}")
                failed = True
        path.write_text(text)
    for path in list(DOCS.glob("*.md")) + [SKILL]:
        if path.name == "EFFECT_MERGE_PLAN.md":
            continue
        for number, line in enumerate(path.read_text().splitlines(), 1):
            for pattern in FORBIDDEN:
                if pattern.search(line):
                    print(f"{path}:{number}: still shows {pattern.pattern}")
                    failed = True
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
