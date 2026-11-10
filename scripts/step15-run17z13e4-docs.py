#!/usr/bin/env python3
"""Step 15, run 17z13e4: agent-docs follow the creature-death defaults and the own-zone-change default.

Rewrites the lines that describe a form the run changed: The Skullspore Nexus's "one or more" (`TriggerOptions.ONE_OR_MORE`,
the total power the event value), Brood of Cockroaches' and Triumph of Saint Katherine's "put into your graveyard"
filter, Laquatus's Champion's "that player gains N life" (`GainLifeRecipient.SOURCE_REMEMBERED_PLAYER`), Lifeline's
intervening "if" (`AnotherCreatureOnBattlefield`, the delayed return's seven-argument constructor), Diabolic
Servitude's chosen-permanent filter and Grave Betrayal's deleted handler. Fails if a doc still names a removed form.
Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("""-> `Triggers.nontokenCreatureDies(YOU_CONTROL)` + `CreateTokenWithTotalDyingCreaturesPowerEffect(CreateTokenEffect("Fungus Dinosaur", 0, 0, GREEN, [FUNGUS, DINOSAUR], ...))`. The collector groups the simultaneous event, snapshots each creature's last-known effective power, and queues one token creation with the summed value (The Skullspore Nexus).""",
         """-> `addTrigger(new TriggerCondition(Triggers.nontokenCreatureDies(YOU_CONTROL)), TriggerOptions.ONE_OR_MORE, CreateTokenWithTotalDyingCreaturesPowerEffect(CreateTokenEffect("Fungus Dinosaur", 0, 0, GREEN, [FUNGUS, DINOSAUR], ...)))`. The "one or more" collector records the total last-known power of those creatures (CR 603.10a) as the entry's event value, which the effect reads (The Skullspore Nexus)."""),
        ("""-> `Triggers.dies(THIS)` + `RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect(1)` (Brood of Cockroaches).""",
         """-> `Triggers.dies(new PermanentOwnedBySourceControllerPredicate(), THIS)` + `RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect(1)` (Brood of Cockroaches; "your graveyard" is the filter: a card goes to its owner's graveyard, so a stolen Brood doesn't trigger)."""),
        ("""`TargetPlayerGainsLifeEffect(N)` on `Triggers.leavesBattlefield(THIS)` | triggers | The ETB marker remembers the player for the linked leaves trigger; if the creature leaves before the ETB resolves, the leaves collector uses the ETB stack entry's target. Laquatus's Champion (TOR) |""",
         """`GainLifeEffect(new Fixed(N), GainLifeRecipient.SOURCE_REMEMBERED_PLAYER)` on `Triggers.leavesBattlefield(THIS)` | triggers | The ETB marker remembers the player; "that player" is read from the source as it last existed, or, if the creature left before the ETB resolved, from the ETB stack entry's target. Laquatus's Champion (TOR), Soul Scourge |"""),
        ("""-> `Triggers.dies(THIS)` + `ExileSourceCardFromGraveyardAndShuffleTopCardsEffect(6)` (Triumph of Saint Katherine, 40K 17).""",
         """-> `Triggers.dies(new PermanentOwnedBySourceControllerPredicate(), THIS)` + `ExileSourceCardFromGraveyardAndShuffleTopCardsEffect(6)` (Triumph of Saint Katherine, 40K 17; "your graveyard" is the filter)."""),
        ("""`RegisterDelayedReturnDyingCreatureUnderControlEffect(... returnUnderOwnersControl=true)`""",
         """`RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true)` (return under its owner's control)"""),
        ("""Grave Betrayal. `DeathTriggerCollectorService#handleOpponentCreatureDelayedReturnUnderControl` stamps the dying card id onto `StackEntry.triggeringCardId`;""",
         """Grave Betrayal. The "a creature an opponent controls dies" default records the dying card as `StackEntry.triggeringCardId`, whatever the effect;"""),
        ("""new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true, false)), "…")` | loyalty ability | Daretti""",
         """new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true)), "…")` | loyalty ability | Daretti"""),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("""- `CreateTokenWithTotalDyingCreaturesPowerEffect(CreateTokenEffect tokenTemplate)` — `Triggers.nontokenCreatureDies(YOU_CONTROL)` trigger marker: one simultaneous death event creates one token whose base power and toughness equal the summed last-known effective power of the controller's nontoken creatures that died. The trigger collector snapshots the total before delegating to the ordinary token-creation handler. Used by The Skullspore Nexus""",
         """- `CreateTokenWithTotalDyingCreaturesPowerEffect(CreateTokenEffect tokenTemplate)` — on a `TriggerOptions.ONE_OR_MORE` "whenever one or more creatures die" ability: one token whose base power and toughness equal the total last-known power of those creatures, the entry's event value (the "one or more" collector records it whatever the effect is). Used by The Skullspore Nexus"""),
    ],
    "EFFECTS_INDEX.md": [
        ("""| `CreateTokenWithTotalDyingCreaturesPowerEffect` | `(CreateTokenEffect tokenTemplate)` | `Triggers.nontokenCreatureDies(YOU_CONTROL)` trigger marker: for one simultaneous death event, snapshots the total last-known effective power of the controller's nontoken creatures and creates one token whose base power and toughness use that total.""",
         """| `CreateTokenWithTotalDyingCreaturesPowerEffect` | `(CreateTokenEffect tokenTemplate)` | on a `TriggerOptions.ONE_OR_MORE` "whenever one or more creatures die" ability: creates one token whose base power and toughness are the total last-known power of those creatures, the entry's event value."""),
        ("""`recipient` defaults to `CONTROLLER`; `TRIGGERING_PERMANENT_CONTROLLER` gives it""",
         """`recipient` defaults to `CONTROLLER`; `SOURCE_REMEMBERED_PLAYER` gives it to the player the source remembered as it entered (Laquatus's Champion's leaves-the-battlefield "that player gains 6 life": the source as it last existed, else its enters ability's target still on the stack); `TRIGGERING_PERMANENT_CONTROLLER` gives it"""),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("""+ Triggers.creatureDies(ANY) `ExileTriggeringCreatureAndReturnSourceToHandEffect` (the collector matches the dying permanent ID to the source's `chosenPermanentId`; the card exiled is the stack entry's triggering card)""",
         """+ `Triggers.creatureDies(new PermanentIsChosenPermanentPredicate(), ANY)` `ExileTriggeringCreatureAndReturnSourceToHandEffect` ("the creature put onto the battlefield with this" is the filter, judged on the creature as it last existed; the card exiled is the stack entry's triggering card)"""),
    ],
    "ARCHITECTURE.md": [
        ("""An ability of one effect has its event data bound by `LegacyTriggerBindings`, the old collectors' handlers keyed by effect class (until step 15); an ability of several effects is one stack object.""",
         """The source's own zone-change ability ("when this dies", "leaves the battlefield", "is put into a graveyard from anywhere", "when you sacrifice this") goes on the stack through one default, `TriggeredAbilityStackService.triggerOwn`, as one stack object that records the event whatever its effects are: the source as it last existed (snapshot, triggering permanent with its attachments, last-known power as the event value, mana value, how it was cast), the card with its new zone's incarnation (CR 400.7) and the card its linked ability exiled; a modal one waits for its mode, a targeted one for its targets (a leaves-the-battlefield one through `SelfTriggeredAbilityTarget`, the others through `DeathTriggerTarget`). Each creature-death watch (`CreatureDeathWatch`) has one default, keyed by the watch alone, that records the death the same way (`DeathTriggerCollectorService.queueDeathAbility`; the "a creature you control" collectors' `recordCreatureDeath`)."""),
        ("""`TriggerCollectorRegistry.dispatch`, `DeathTriggerCollectorService.dispatchCreatureDeath` and `LegacyTriggerBindings` pick a handler by the effect's own class, then the watch's default,""",
         """`TriggerCollectorRegistry.dispatch` picks a handler by the effect's own class, then the watch's default (the creature-death watches and the source's own zone changes have only their default),"""),
    ],
    "EFFECTS_INDEX.md#3": [
        ("""`AnyPlayerControlsPermanent(filter)`, `AnyPlayerControlsPermanentCount(minCount, filter[, excludeSource])` (N+ across all battlefields; the optional source exclusion supports "other" ETB counts),""",
         """`AnyPlayerControlsPermanent(filter)`, `AnyPlayerControlsPermanentCount(minCount, filter[, excludeSource])` (N+ across all battlefields; the optional source exclusion supports "other" ETB counts), `AnotherCreatureOnBattlefield()` (a creature other than the triggering permanent; as a death triggers the ability the creatures that died with it count, CR 603.10a: Lifeline's intervening "if another creature is on the battlefield"),"""),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md#2": [
        ("""| "Whenever a white creature dies, return it to the battlefield under its owner's control""",
         """| "Whenever a creature dies, if another creature is on the battlefield, return the first card to the battlefield under its owner's control at the beginning of the next end step." | `Triggers.creatureDies(ANY)`, `TriggerOptions.DEFAULT.withInterveningIf(new AnotherCreatureOnBattlefield())`, `RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true)` | triggers | Lifeline. The "if" looks back at the creatures that died with it as it triggers and is checked again as it resolves (CR 603.4): when every creature dies at once it triggers and does nothing |
| "Whenever a white creature dies, return it to the battlefield under its owner's control"""),
    ],
    "EFFECTS_INDEX.md#2": [
        ("""new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true, false)), "…")`, read by""",
         """new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true)), "…")`, read by"""),
    ],
    "EFFECTS_QUICK_REFERENCE.md#2": [
        ("""new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true, false)), "…")`, read by""",
         """new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true)), "…")`, read by"""),
    ],
    "CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md": [
        ("""new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true, false)), "…")`""",
         """new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true)), "…")`"""),
    ],
}

FORBIDDEN = [
    "handleOpponentCreatureDelayedReturnUnderControl",
    "null, null, false, true, false)",
    "requireAnotherCreature",
    "LegacyTriggerBindings",
    "the leaves collector uses the ETB stack entry's target",
]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name.split("#")[0]
        text = path.read_text()
        for old, new in edits:
            if new in text and (old not in text or old in new):
                continue
            assert old in text, (name, old[:80])
            text = text.replace(old, new)
        path.write_text(text)
    bad = []
    for path in sorted(DOCS.glob("*.md")):
        text = path.read_text()
        bad += [f"{path.name}: {needle}" for needle in FORBIDDEN if needle in text]
    if bad:
        print("\n".join(bad))
        sys.exit(1)
    print("docs updated")


if __name__ == "__main__":
    main()
