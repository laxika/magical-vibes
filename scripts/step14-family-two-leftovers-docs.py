#!/usr/bin/env python3
"""Step 14, run 16d of effect-slot-refactor.md: the agent-docs passages about family 2's legacy view.

No spell, land-play or activation ability has a legacy view any more: every reader of family 2 reads the ability
itself and judges its options. A granted ability's "triggers only once each turn" is its TriggerLimit, kept per
granting permanent, so `OncePerTurnTriggerEffect.perGrant` is only for the granted abilities of families still on the
legacy view (Well Rested). The Fourth Doctor's two once-each-turn wrappers are one ability of both conditions. Each
edit is an exact replacement; running the script again changes nothing.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent

EDITS = {
    "agent-docs/ARCHITECTURE.md": [
        ("reading the abilities through `SpellAbilityWatch` (one constant per old slot, naming its trigger and the zone "
         "the collector reads: `effectsOf(card)`, `registrationsOf(card)`, `dispatchWatch`), and their handlers are "
         "keyed by watch with `@CollectsSpellAbilityTrigger`. An unfiltered cast or land-play ability is read through "
         "`Card.getLegacyTriggerEffects(trigger, zone)` (the legacy view, keyed by `Trigger.watchKey()`), which hands "
         "the collectors the intervening \"if\" and the limit as the wrappers they judge (`ConditionalEffect`, "
         "`OncePerTurnTriggerEffect`).",
         "reading the abilities through `SpellAbilityWatch` (one constant per old slot, naming its trigger and the zone "
         "the collector reads, and `dispatchWatch`), and their handlers are keyed by watch with "
         "`@CollectsSpellAbilityTrigger`. No spell, land-play or activation ability has a legacy view "
         "(`Trigger.collectedAsAbility()`: `TriggeredAbility.legacyEffects()`, the `getLegacy…` getters and "
         "`SpellAbilityWatch.effectsOf` throw for them), so every reader reads the ability itself. An unfiltered one "
         "(no spell filter, land filter or zone) is collected by `TriggerCollectionService.collectUnfilteredAbility`, "
         "which checks the event condition and the intervening \"if\" as it triggers, judges and marks the limit by "
         "`TriggeredAbility.limitKey()` (`collectLimitedAbility`), and hands each effect to the watch's handlers (the "
         "event bindings), the intervening \"if\" wrapped around it as the `ConditionalEffect` that checks it again "
         "as it resolves; the \"when you cast this spell\" and \"when this spell is countered\" readers and the "
         "suspended-card reader judge the options the same way. The \"when you cast this spell\" reader still puts "
         "the plain effects of all the card's such abilities on the stack as one object, as cards wrote one ability "
         "as several. A granted ability with a limit is the granting permanent's instance of it "
         "(`TriggeredAbility.instance()`, set by `GrantTriggeredAbilityEffectHandler`), so each grant keeps its own "
         "limit (CR 113.2c)."),
    ],
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("`OncePerTurnTriggerEffect.perGrant(CardEffect)` is for a granted ability (`GrantTriggeredAbilityEffect`): "
         "each granting permanent gets its own once-per-turn limit on the receiving permanent (Folk Hero); only for "
         "slots whose collector marks via `OncePerTurnTriggerSupport`.",
         "A granted ability (`GrantTriggeredAbilityEffect`) that \"triggers only once each turn\" is "
         "`new TriggeredAbility(trigger, TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), effects...)`: "
         "each granting permanent grants its own instance with its own limit (Folk Hero, Tyvar the Bellicose). "
         "`OncePerTurnTriggerEffect.perGrant(CardEffect)` is left only on granted abilities whose collectors still "
         "read the legacy view (Well Rested); don't author it on a spell, land-play or activation trigger."),
        ("- `CreateFoodWhenPlayingCardFromTopOfLibraryEffect(CardPredicate)` - trigger marker for creating one Food "
         "when the controller plays a matching card from the library top through the paired once-per-turn permission "
         "(The Fourth Doctor, WHO 2).",
         "- `CreateFoodWhenPlayingCardFromTopOfLibraryEffect(CardPredicate)` - trigger marker for creating one Food "
         "when the controller plays a matching card from the library top through the paired once-per-turn permission: "
         "one ability of both conditions, `addTrigger(List.of(Triggers.youCastSpell(), Triggers.youPlayLand()), "
         "TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), effect)` (The Fourth Doctor, WHO 2)."),
    ],
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("`Triggers.youCastSpell()`/`Triggers.youPlayLand() OncePerTurnTriggerEffect(CreateFoodWhenPlayingCardFromTop"
         "OfLibraryEffect(CardIsHistoricPredicate()))` | STATIC + cast/land triggers |",
         "`addTrigger(List.of(Triggers.youCastSpell(), Triggers.youPlayLand()), TriggerOptions.DEFAULT.withLimit("
         "TriggerLimit.ONCE_EACH_TURN), CreateFoodWhenPlayingCardFromTopOfLibraryEffect(CardIsHistoricPredicate()))` "
         "| STATIC + one cast/land trigger |"),
    ],
}


def main():
    changed = 0
    for relative, edits in EDITS.items():
        path = ROOT / relative
        text = path.read_text()
        original = text
        for old, new in edits:
            if new in text:
                continue
            if old not in text:
                sys.exit(f"{relative}: neither the old nor the new text of an edit is there:\n{old[:200]}")
            text = text.replace(old, new)
        if text != original:
            path.write_text(text)
            changed += 1
    print(f"{changed} files changed")


if __name__ == "__main__":
    main()
