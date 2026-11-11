#!/usr/bin/env python3
"""Step 15, runs 17z16c4 and 17z16c3b: the agent-docs lines for the authoring forms the runs changed.

Rewrites single lines (matched by a unique fragment of the old line) of `agent-docs/`: "create a token. When you
do, ... that token" (`CreateTokenThenEffect.aboutThatToken`), a mana ability's "when you do"
(`QueueReflexiveAbilityEffect` after the mana), exert as the creature attacks (the exertion first, then one
`QueueReflexiveAbilityEffect`), "choose up to that many —" of a pay-up-to-N-times ability
(`ChooseOneAtTriggerTimeEffect(modes, new XValue())`), Vial Smasher's effect, Curse of the Werefox's optional
target, and the architecture note on exert and on the mode route of a reflexive ability.

Idempotent; fails if a line is not found.

Usage: python3 scripts/step15-run17z16c4-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "agent-docs"

# file -> [(unique fragment of the old line, the whole new line or None to delete it, marker of the new line)]
EDITS = {
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("Any non-mana effects in its list are treated as reflexive \"when you do\" riders resolved inline",
         "An ability that produces mana and has no target/loyalty cost is a **mana ability** (resolves immediately, no stack). The other effects in its list resolve inline with the mana in `ActivatedAbilityExecutionService.doResolveManaAbility`. Supported inline effects include `GainLifeEffect`, `DealDamageToPlayersEffect` with recipient `CONTROLLER`, `PutCountersOnSelfEffect` (Pyramid of the Pantheon: `{2}, {T}: Add one mana of any color. Put a brick counter on this artifact.` — the counter is placed on the source, respecting `cantHaveCounters`), and `CopyNextInstantOrSorceryCastThisTurnEffect` (Ether: `{T}, Exile this artifact: Add {U}. When you next cast an instant or sorcery spell this turn, copy that spell.`). A `ConditionalEffect` wrapping one of those is also supported — the condition is evaluated at mana-ability resolution and the wrapped effect runs only when met (Avid Reclaimer: `{T}: Add {G} or {U}. If you control a Nissa planeswalker, you gain 2 life.`). **\"Add [mana]. When you do, [effect]\"** is a reflexive triggered ability, which can't add mana and so is not part of the mana ability (CR 603.12, 605.5a): write `new QueueReflexiveAbilityEffect(effect)` after the mana effect (Rubble Rouser: `QueueReflexiveAbilityEffect(DealDamageToPlayersEffect(1, EACH_OPPONENT))`; Shaun & Rebecca, Agents: `QueueReflexiveAbilityEffect(MillEffect(2, MillRecipient.CONTROLLER))`), and `ReflexiveManaAbilityRiderEffectHandler` queues it with the other mana-ability triggers. Never write the bare effect: a `MillEffect` (any recipient) or an effect that draws from a library, such as `DrawCardEffect`, makes the ability use the stack instead, and an effect placed on a mana ability but not handled in `doResolveManaAbility` is silently dropped. To support a new inline effect, add a branch there.",
         "ReflexiveManaAbilityRiderEffectHandler"),
    ],
    "ARCHITECTURE.md": [
        ("and queues the resulting reflexive ability for target selection and normal stack resolution.",
         "and puts the \"when you do\" the card writes after the exertion (`QueueReflexiveAbilityEffect`) on the stack as one reflexive ability. The exertion itself (`SkipNextUntapEffect` with `controllerStepOnly` on the source) fires \"whenever you exert a creature\" (`TriggerCollectionService.checkExertTriggers`) wherever it resolves. A modal reflexive ability's modes are chosen by one service whichever handler made the ability (`ReflexiveModalChoiceService.beginModeChoice`: \"choose up to that many\" is `TriggeredModalEffect.maximumChoices()`, read against the ability's record).",
         "ReflexiveModalChoiceService.beginModeChoice"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `DealDamageToRandomOpponentOrTheirPlaneswalkerEffect` | `(DynamicAmount)` / `(int)` |",
         "| `DealDamageToRandomOpponentOrTheirPlaneswalkerEffect` | — | **Gone** (effect-slot-refactor.md step 15, run 17z16c4): it put a second, targeting entry on the stack for one ability. Write `DealDamageToRandomOpponentOrPlaneswalkerEffect(amount)`, which makes the choice between that player and their planeswalkers as the ability resolves (Vial Smasher the Fierce: `new TargetSpellManaValue()`) |",
         "it put a second, targeting entry on the stack for one ability"),
        ("| `PayManaUpToNTimesEffect` | `(String manaCost, int maximumPayments, CardEffect thenEffect)` |",
         "| `PayManaUpToNTimesEffect` | `(String manaCost, int maximumPayments, CardEffect thenEffect)` | on resolution, controller chooses up to the capped number of fixed-cost payments; if at least one payment is made, the follow-up effect is put on the stack as a reflexive ability whose X is the number of payments. \"Choose up to that many —\" is `new ChooseOneAtTriggerTimeEffect(modes, new XValue())` as the follow-up: the most modes that may be chosen is read from the reflexive ability's X as it is put on the stack. Used by Tranquil Frillback, Hawkeye, Master Marksman |",
         "new ChooseOneAtTriggerTimeEffect(modes, new XValue())"),
        ("| `CreateTokenThenEffect` | `(CreateTokenEffect tokenEffect, CardEffect thenEffect)` |",
         "| `CreateTokenThenEffect` | `(CreateTokenEffect tokenEffect, CardEffect thenEffect)` / `aboutThatToken(tokenEffect, thenEffect)` | creates the wrapped tokens and, if at least one token was created, puts the follow-up on the stack as a reflexive ability. Use when the wording is \"create [tokens]. When you do, [thenEffect]\" and the follow-up target must be chosen after token creation. The follow-up's target restrictions come from its own `targetSpec()`. When the follow-up is about \"that token\" (\"attach it to target creature\", \"... attached to that token\") write `CreateTokenThenEffect.aboutThatToken(...)`: one reflexive ability for each token created (a replacement effect may make more), each with its token as the permanent its effect acts for (the entry's source permanent) and its own target |",
         "aboutThatToken(tokenEffect, thenEffect)"),
        ("| `CreateTokenAttachedToTargetThenEffect` | `(CreateTokenEffect tokenEffect, CardEffect thenEffect[, PlayerRelation targetControllerRelation])` |",
         "| `CreateTokenAttachedToTargetThenEffect` | `(CreateTokenEffect tokenEffect, CardEffect thenEffect[, PlayerRelation targetControllerRelation])` | creates and attaches the token to the targeted creature, then puts the follow-up on the stack as a reflexive ability only if creation succeeded; the follow-up target is chosen after creation, and \"up to one target\" is the follow-up's own (`hasOptionalTarget()`, e.g. `EnteringCreatureFightsTargetCreatureEffect(predicate, true)`). Used by Curse of the Werefox |",
         "\"up to one target\" is the follow-up's own"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `CreateTokenAttachedToTargetThenEffect(CreateTokenEffect token, CardEffect thenEffect[, PlayerRelation targetControllerRelation])` —",
         "- `CreateTokenAttachedToTargetThenEffect(CreateTokenEffect token, CardEffect thenEffect[, PlayerRelation targetControllerRelation])` — creates and attaches the supplied token to the targeted creature, then queues the follow-up as a reflexive ability only if the token was created; the follow-up target is chosen after creation, and \"up to one target\" is the follow-up's own (`EnteringCreatureFightsTargetCreatureEffect(predicate, true)`)",
         "\"up to one target\" is the follow-up's own"),
        ("- `PayManaUpToNTimesEffect(String manaCost, int maximumPayments, CardEffect thenEffect)` —",
         "- `PayManaUpToNTimesEffect(String manaCost, int maximumPayments, CardEffect thenEffect)` — resolution-time fixed-cost payment capped at `maximumPayments`; when one or more payments are made, queues `thenEffect` as a reflexive ability whose X is the number of payments. \"Choose up to that many —\": `new ChooseOneAtTriggerTimeEffect(modes, new XValue())` as `thenEffect`. Tranquil Frillback; Hawkeye, Master Marksman.",
         "new ChooseOneAtTriggerTimeEffect(modes, new XValue())"),
        ("- `DealDamageToRandomOpponentOrTheirPlaneswalkerEffect(DynamicAmount)`; `(int)` —",
         "- `DealDamageToRandomOpponentOrTheirPlaneswalkerEffect` — **gone** (step 15, run 17z16c4): write `DealDamageToRandomOpponentOrPlaneswalkerEffect(amount)`, one stack entry whose choice between that player and their planeswalkers is made as it resolves (Vial Smasher the Fierce: `new TargetSpellManaValue()`)",
         "one stack entry whose choice between that player and their planeswalkers"),
        ("- Exert as it attacks: `Triggers.attacks() MayEffect(SequenceEffect.of(SkipNextUntapEffect(SELF, null, 1, false, false, true), <when-you-do effects>))`.",
         "- Exert as it attacks: `Triggers.attacks()` + `MayEffect(SequenceEffect.of(new SkipNextUntapEffect(SELF, null, 1, false, false, true), new QueueReflexiveAbilityEffect(<when-you-do effect>)), prompt)` — the exertion first, then the card's \"when you do\" as ONE reflexive ability (several effects: `QueueReflexiveAbilityEffect(SequenceEffect.of(...))`). Accepting exerts immediately (skip untap + `Triggers.youExertCreature()` triggers, fired by the exertion itself) and puts the reflexive ability on the stack, its target chosen then (Ahn-Crop Crasher, Glorybringer, Devoted Crop-Mate; untargeted: Glory-Bound Initiate, Watchful Naga). Never write the payoff as a bare step of the sequence: it would resolve with the exertion instead of going on the stack. Plain exert with no payoff: `MayEffect(SkipNextUntapEffect(SELF, null, 1, false, false, true))` (Battlefield Scavenger).",
         "the exertion first, then the card's \"when you do\" as ONE reflexive ability"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`.nthThisTurn(1)` on the cast trigger with `new DealDamageToRandomOpponentOrTheirPlaneswalkerEffect(new TargetSpellManaValue())`",
         "| \"Whenever you cast your first spell each turn, choose an opponent at random. Vial Smasher deals damage equal to that spell's mana value to that player or a planeswalker that player controls.\" | `.nthThisTurn(1)` on the cast trigger with `new DealDamageToRandomOpponentOrPlaneswalkerEffect(new TargetSpellManaValue())` | `Triggers.youCastSpell()` | Vial Smasher the Fierce (FCA 59); one ability, one stack entry: the opponent is selected randomly as it resolves and the player/planeswalker choice is made then and is not a target; the damage is the spell's mana value as the ability resolves, as it last existed on the stack if it was countered |",
         "one ability, one stack entry: the opponent is selected randomly as it resolves"),
        ("`target(TargetFilters.creatureYouControl()).addSpellEffect(CreateTokenAttachedToTargetThenEffect(monsterRoleToken(), EnteringCreatureFightsTargetCreatureEffect(opponent-creature predicate)))`",
         "| \"Create a Monster Role token attached to target creature you control. When you do, that creature fights up to one target creature you don't control.\" | `target(TargetFilters.creatureYouControl()).addSpellEffect(CreateTokenAttachedToTargetThenEffect(monsterRoleToken(), EnteringCreatureFightsTargetCreatureEffect(opponent-creature predicate, true)))` | SPELL | Curse of the Werefox (`WOE`). The Role is attached before the reflexive fight trigger is put on the stack, and the fight target is chosen only after the token is created; \"up to one\" is the fight effect's own `optionalTarget` (`true`), so with no creature to target the ability goes on the stack with none |",
         "EnteringCreatureFightsTargetCreatureEffect(opponent-creature predicate, true)"),
        ("`ActivatedAbility(true, null, List.of(AwardManaEffect(COLORLESS), MillEffect(2, CONTROLLER)), ...)`",
         "| \"When Shaun & Rebecca enters, search your graveyard, hand, and library for a card named The Animus and put it onto the battlefield, then shuffle. {T}: Add {C}. When you do, mill two cards.\" | `SearchZonesForCardNamedToBattlefieldEffect(\"The Animus\")` on `Triggers.enters(ObjectRelation.THIS)` + `ActivatedAbility(true, null, List.of(AwardManaEffect(COLORLESS), QueueReflexiveAbilityEffect(MillEffect(2, CONTROLLER))), ...)` | ETB + mana ability | Shaun & Rebecca, Agents (ACR 64). \"When you do\" after the mana is a reflexive triggered ability (CR 605.5a): write it as `QueueReflexiveAbilityEffect`, and it waits for the stack with the other mana-ability triggers. |",
         "QueueReflexiveAbilityEffect(MillEffect(2, CONTROLLER))"),
        ("`MayEffect(SequenceEffect.of(BoostSelfEffect(1,1), MakeCreatureBlockableOnlyByFilterThisTurnEffect(PermanentPowerAtLeastPredicate(3), \"creatures with power 3 or greater\", true), SkipNextUntapEffect(SELF, null, 1, false, false, true)), prompt)`",
         "| \"You may exert this creature as it attacks. When you do, it gets +1/+1 until end of turn and can't be blocked by creatures with power 2 or less this turn\" | `MayEffect(SequenceEffect.of(SkipNextUntapEffect(SELF, null, 1, false, false, true), QueueReflexiveAbilityEffect(SequenceEffect.of(BoostSelfEffect(1,1), MakeCreatureBlockableOnlyByFilterThisTurnEffect(PermanentPowerAtLeastPredicate(3), \"creatures with power 3 or greater\", true)))), prompt)` | `Triggers.attacks()` | Rhonas's Stalwart. Exert-as-attack pattern (Hooded Brawler): the exertion first, then the \"when you do\" as one reflexive ability; \"can't be blocked by power 2 or less\" is only-by power 3 or greater (Steel Leaf Champion). `selfTargeting=true` on the block restriction |",
         "QueueReflexiveAbilityEffect(SequenceEffect.of(BoostSelfEffect(1,1)"),
        ("`CreateTokenThenEffect(CreateTokenEffect(...), AttachCreatedEquipmentToTargetCreatureEffect())` | Triggers.enters(ObjectRelation.THIS) | Dáin Ironfoot (HOB 91).",
         "| \"When you do, attach it to target creature you control\" after creating an Equipment token | `CreateTokenThenEffect.aboutThatToken(CreateTokenEffect(...), AttachCreatedEquipmentToTargetCreatureEffect())` | Triggers.enters(ObjectRelation.THIS) | Dáin Ironfoot (HOB 91). `aboutThatToken` makes one reflexive ability for each created token with that token's permanent id as the reflexive source, so the target is chosen after creation and the exact Equipment token is attached if it remains on the battlefield. |",
         "CreateTokenThenEffect.aboutThatToken(CreateTokenEffect(...), AttachCreatedEquipmentToTargetCreatureEffect())"),
        ("`CreateTokenThenEffect(CreateTokenEffect(...), ReturnTargetEquipmentFromGraveyardAndAttachToCreatedTokenEffect())` | Triggers.dealsCombatDamageToPlayer() |",
         "| \"When you do, return target Equipment card from your graveyard to the battlefield, then attach it to that token\" | `CreateTokenThenEffect.aboutThatToken(CreateTokenEffect(...), ReturnTargetEquipmentFromGraveyardAndAttachToCreatedTokenEffect())` | Triggers.dealsCombatDamageToPlayer() | Ratonhnhaké꞉ton (ACR 62). The Equipment target is chosen after the Assassin token is created, and the reflexive ability retains that token's permanent id for attachment (`aboutThatToken`: one ability per created token). |",
         "CreateTokenThenEffect.aboutThatToken(CreateTokenEffect(...), ReturnTargetEquipmentFromGraveyardAndAttachToCreatedTokenEffect())"),
        ("`PayManaUpToNTimesEffect(\"{G}\", 3, ChooseOneEffect.oneOrMore(options))`",
         "| \"When this creature enters, you may pay {G} up to three times. When you pay this cost one or more times, choose up to that many —\" | `PayManaUpToNTimesEffect(\"{G}\", 3, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(options, false, 0, 3, false, null), new XValue()))` | Triggers.enters(ObjectRelation.THIS) | Tranquil Frillback (MAT 24). The number paid is the reflexive ability's X, and \"up to that many\" is read from it as the ability is put on the stack; the modes and their targets are chosen then |",
         "new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(options, false, 0, 3, false, null), new XValue())"),
    ],
}


def main():
    changed = 0
    for name, edits in EDITS.items():
        path = ROOT / name
        lines = path.read_text().split("\n")
        original = list(lines)
        for fragment, replacement, marker in edits:
            if any(marker in line for line in lines):
                continue
            matches = [index for index, line in enumerate(lines) if fragment in line]
            if len(matches) != 1:
                sys.exit(f"{name}: {len(matches)} lines contain: {fragment}")
            lines[matches[0]] = replacement
        if lines != original:
            path.write_text("\n".join(lines))
            changed += 1
            print(f"updated agent-docs/{name}")
    print(f"{changed} files changed")


if __name__ == "__main__":
    main()
