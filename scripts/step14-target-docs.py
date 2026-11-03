#!/usr/bin/env python3
"""Step 14, run 10 of effect-slot-refactor.md: the agent-docs rows of the becomes-the-target abilities show the
trigger's event, filter and options instead of the trigger-only wrappers, and the wrapper classes' entries say not to
author them. Exact replacements; one that doesn't match stops the script.

Usage: python3 scripts/step14-target-docs.py [--dry-run]
"""
import sys

DONT = ("**Don't author** (step 14 of effect-slot-refactor.md): ")

EDITS = {
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("`Triggers.becomesTargetOfSpellOrAbility() TriggeringSpellControllerConditionalEffect(OncePerTurnTriggerEffect("
         "CreateTokenEffect(1/1 white Mouse)))`",
         "`Triggers.becomesTargetOfYourSpellOrAbility()`, `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)`"
         ", `CreateTokenEffect(1/1 white Mouse)`"),
        ("`Triggers.becomesTargetOfSpellOrAbility() TriggeringSpellControllerConditionalEffect(OncePerTurnTriggerEffect("
         "PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE)))`",
         "`Triggers.becomesTargetOfYourSpellOrAbility()`, `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)`"
         ", `PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE)`"),
        ("`GrantTriggeredAbilityToCastSpellEffect(new TriggeredAbility(Triggers.becomesTargetOfSpellOrAbility(), "
         "TriggeringSpellOpponentControllerConditionalEffect(MayEffect(FlickerEffect(SELF, IMMEDIATE, owner control), "
         "...))))` | Triggers.youCastThisSpell() + Triggers.becomesTargetOfSpellOrAbility() |",
         "`GrantTriggeredAbilityToCastSpellEffect(new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(), "
         "MayEffect(FlickerEffect(SELF, IMMEDIATE, owner control), ...)))` | Triggers.youCastThisSpell() + "
         "Triggers.becomesTargetOfOpponentSpellOrAbility() |"),
        ("`target(TargetFilters.nonlandPermanentAnOpponentControls()).addTrigger(Triggers."
         "creatureYouControlBecomesTargetOfOpponentSpellOrAbility(), TriggeringPermanentConditionalEffect("
         "PermanentHasSubtypePredicate(DRAGON), DestroyTargetPermanentEffect()))`",
         "`target(TargetFilters.nonlandPermanentAnOpponentControls()).addTrigger(Triggers."
         "creatureYouControlBecomesTargetOfOpponentSpellOrAbility().withFilter(PermanentHasSubtypePredicate(DRAGON)), "
         "DestroyTargetPermanentEffect())`"),
        ("| `new TriggeringSpellControllerConditionalEffect(new DrawCardEffect())` | `Triggers.becomesTargetOfSpell()` | "
         "Stormchaser Drake. The wrapper filters the normal becomes-target trigger to spells controlled by the source "
         "permanent's controller |",
         "| `new DrawCardEffect()` | `Triggers.becomesTargetOfYourSpell()` | Stormchaser Drake. The trigger's event is "
         "about spells the source's controller controls |"),
        ("| `AbilityTargetChoiceTriggerEffect(OncePerTurnTriggerEffect(DrawCardEffect()))` | "
         "`Triggers.playerChoosesTargets()` | Loki, God of Mischief (MSH). The target-choice collector filters the event "
         "to activated or triggered abilities controlled by the source controller and to player/permanent targets, then "
         "applies the once-per-turn marker |",
         "| `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)`, `DrawCardEffect()` | "
         "`Triggers.playerOrPermanentBecomesTargetOfYourAbility()` | Loki, God of Mischief (MSH). The trigger's event "
         "is an activated or triggered ability the source's controller controls targeting a player or permanent; "
         "\"triggers only once each turn\" is the option |"),
    ],
    "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("`Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility()` + `TriggeringPermanentConditionalEffect("
         "PermanentHasSubtypePredicate(DRAGON), DestroyTargetPermanentEffect())`",
         "`Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility().withFilter(PermanentHasSubtypePredicate("
         "DRAGON))` + `DestroyTargetPermanentEffect()`"),
    ],
    "agent-docs/CARD_PATTERNS_CREATURES_ETB.md": [
        ("`addMorph(faceUpCost)` supplies the disguise alternate cost; wrap `CounterUnlessPaysEffect(2)` in "
         "`ConditionalEffect(new SourceIsFaceDown(), ...)` on `Triggers.becomesTargetOfOpponentSpellOrAbility()` so the "
         "ward exists only while face down. |",
         "`addMorph(faceUpCost)` supplies the disguise alternate cost. The engine gives a permanent cast face down with "
         "disguise ward {2} (the card's `DISGUISE` keyword); don't author a ward ability for it. |"),
    ],
    "agent-docs/TRIGGER_SLOT_TARGETING.md": [
        ("For `Triggers.becomesTargetOfSpell()`, wrap an effect in `TriggeringSpellControllerConditionalEffect`\n"
         "when the trigger should fire only for spells controlled by the source permanent's controller. The\n"
         "wrapper is evaluated while the becomes-target trigger is collected and then removed before the\n"
         "effect is put on the stack.",
         "\"Whenever this permanent becomes the target of a spell [or ability] you control\" is\n"
         "`Triggers.becomesTargetOfYourSpell()` / `Triggers.becomesTargetOfYourSpellOrAbility()` (valiant, with\n"
         "`TriggerLimit.ONCE_EACH_TURN` for \"for the first time each turn\"). The collectors of\n"
         "`becomesTargetOfSpell()` / `becomesTargetOfSpellOrAbility()` read them, and the legacy view hands them\n"
         "the `TriggeringSpellControllerConditionalEffect` those collectors judge as the trigger is collected."),
        ("Target-choice event; the chosen spell/ability is carried as non-targeting `triggeringCardId`. "
         "`AbilityTargetChoiceTriggerEffect` narrows this event to an activated or triggered ability controlled by the "
         "source controller that targets a player or permanent |",
         "Target-choice event; the chosen spell/ability is carried as non-targeting `triggeringCardId`. "
         "`Triggers.playerOrPermanentBecomesTargetOfYourAbility()` (Loki) narrows this event to an activated or "
         "triggered ability controlled by the source controller that targets a player or permanent; this collector "
         "reads it |"),
    ],
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("- `TriggeringSpellOpponentControllerConditionalEffect(CardEffect)` - trigger-only wrapper for "
         "`Triggers.becomesTargetOfSpellOrAbility()`; fires only when the triggering spell or ability was controlled by "
         "an opponent (Lae'zel, Githyanki Warrior, HBG 2)\n", ""),
        ("- `TriggeringSpellControllerConditionalEffect(CardEffect)` — trigger-only wrapper for "
         "`Triggers.becomesTargetOfSpell()`; fires only when the triggering spell was controlled by the source's "
         "controller (Stormchaser Drake)",
         "- `TriggeringSpellControllerConditionalEffect(CardEffect)` — " + DONT + "\"of a spell [or ability] you "
         "control\" is the trigger `Triggers.becomesTargetOfYourSpell()` / `becomesTargetOfYourSpellOrAbility()`; the "
         "legacy view gives this wrapper to the collectors that still judge it (Stormchaser Drake, the valiant cards)"),
        ("- `AbilityTargetChoiceTriggerEffect(CardEffect)` — trigger-only wrapper for an ability controlled by this "
         "permanent's controller targeting a player or permanent; the wrapper is consumed by `checkTargetChoiceTriggers` "
         "and queues its wrapped effect. Loki, God of Mischief pairs it with "
         "`OncePerTurnTriggerEffect(DrawCardEffect())`",
         "- `AbilityTargetChoiceTriggerEffect(CardEffect)` — " + DONT + "\"a player or permanent becomes the target of "
         "an ability you control\" is the trigger `Triggers.playerOrPermanentBecomesTargetOfYourAbility()`; the legacy "
         "view gives this wrapper to `checkTargetChoiceTriggers` (Loki, God of Mischief, with "
         "`TriggerLimit.ONCE_EACH_TURN`)"),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("| `TriggeringSpellControllerConditionalEffect` | `(CardEffect wrapped)` | Trigger-only wrapper for "
         "`Triggers.becomesTargetOfSpell()`: fires only when the triggering spell's controller matches the source "
         "permanent's controller |",
         "| `TriggeringSpellControllerConditionalEffect` | `(CardEffect wrapped)` | " + DONT + "\"of a spell [or "
         "ability] you control\" is the trigger `Triggers.becomesTargetOfYourSpell()` / "
         "`becomesTargetOfYourSpellOrAbility()`; the legacy view gives this wrapper to the becomes-the-target "
         "collectors that still judge it |"),
        ("| `TriggeringSpellOpponentControllerConditionalEffect` | `(CardEffect wrapped)` | Trigger-only wrapper for "
         "`Triggers.becomesTargetOfSpellOrAbility()`: fires only when the triggering spell or ability is controlled by "
         "an opponent of the source permanent's controller. | Lae'zel, Githyanki Warrior (HBG 2) |\n", ""),
        ("| `AbilityTargetChoiceTriggerEffect` | `(CardEffect wrapped)` | Trigger-only target-choice filter for an "
         "activated or triggered ability controlled by this permanent's controller that targets a player or permanent; "
         "the target-choice collector unwraps it before queuing the wrapped effect. Loki, God of Mischief pairs it with "
         "`OncePerTurnTriggerEffect(DrawCardEffect())` |",
         "| `AbilityTargetChoiceTriggerEffect` | `(CardEffect wrapped)` | " + DONT + "\"a player or permanent becomes "
         "the target of an ability you control\" is the trigger `Triggers.playerOrPermanentBecomesTargetOfYourAbility()`"
         "; the legacy view gives this wrapper to the target-choice collector (Loki, God of Mischief, with "
         "`TriggerLimit.ONCE_EACH_TURN`) |"),
    ],
}


def main():
    dry = "--dry-run" in sys.argv
    count = 0
    for path, edits in EDITS.items():
        src = open(path).read()
        for old, new in edits:
            if src.count(old) != 1:
                sys.exit("%s: expected one match of:\n%s" % (path, old))
            src = src.replace(old, new)
            count += 1
        if not dry:
            open(path, "w").write(src)
    print("%d replacements" % count)


if __name__ == "__main__":
    main()
