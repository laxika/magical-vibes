#!/usr/bin/env python3
"""Step 15, run 17z13e5b: agent-docs follow the graveyard, sacrifice, milled and exiled defaults.

The cards say in their triggers what the class-keyed handlers judged (Desert Warfare's and Oglor's "from your hand or
library" as `Triggers.putIntoGraveyardFrom(Set<Zone>, …)`, Glowing One's per-card mill trigger, Yuna's "another
permanent you control", Pia's Revolution's "nontoken artifact put into your graveyard", Magnetic Mine's and Captain
Marvel's "another" / "other than this"), and `RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect` has no filter.
Fails if a doc still describes the old form. Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_OWN) "
         "PerpetuallyGrantTriggeredAbilityEffect(CreateTokenEffect.blackZombie(1).withTapped(true))`",
         "`Triggers.putIntoGraveyardFrom(Set.of(Zone.LIBRARY, Zone.HAND), Triggers.NONTOKEN_CREATURE, "
         "ObjectRelation.YOU_OWN) PerpetuallyGrantTriggeredAbilityEffect(CreateTokenEffect.blackZombie(1)"
         ".withTapped(true))`"),
        ("The perpetual ability is keyed to the affected card identity and only collects hand/library creature cards; "
         "its leaves-graveyard trigger survives later zone changes.",
         "The perpetual ability is keyed to the affected card identity (the stack entry's triggering card); \"from "
         "your library or hand\" is the trigger's `from` zones. Its leaves-graveyard trigger survives later zone "
         "changes."),
        ("Glowing One (PIP 76); the mill collector queues one separate life-gain trigger per nonland card milled.",
         "Glowing One (PIP 76); the trigger has the default (each) multiplicity, so it triggers once for each nonland "
         "card milled (CR 603.2c), not `TriggerOptions.ONE_OR_MORE`."),
        ("| `Triggers.dies(PermanentHasCountersPredicate(ANY), ObjectRelation.ANY)` | Yuna, Grand Summoner (FIC 8). "
         "The effect checks the dying permanent's last-known controller,",
         "| `Triggers.dies(PermanentHasCountersPredicate(ANY), ObjectRelation.ANOTHER_YOU_CONTROL)` | Yuna, Grand "
         "Summoner (FIC 8). The effect checks the dying permanent's last-known controller,"),
        ("`target(PlayerPredicateTargetFilter(OPPONENT)).addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY), "
         "ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect(3))` | `Triggers.dies(Triggers.ARTIFACT, "
         "ObjectRelation.ANY)` | Pia's Revolution. \"That card\" is the stack entry's triggering card; token artifacts "
         "are filtered out.",
         "`target(PlayerPredicateTargetFilter(OPPONENT)).addTrigger(Triggers.dies(Triggers.NONTOKEN_ARTIFACT, "
         "ObjectRelation.YOU_OWN), ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect(3))` | "
         "`Triggers.dies(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.YOU_OWN)` | Pia's Revolution. \"That card\" is the "
         "stack entry's triggering card, in the graveyard incarnation the ability triggered for (CR 400.7)."),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect(CardPredicate)` — trigger effect for a matching "
         "card put into the controller's graveyard from hand or library, or a matching sacrificed permanent.",
         "- `RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect()` — trigger effect for a card put into the "
         "controller's graveyard, or a sacrificed permanent, the trigger's filter saying which (Desert Warfare's "
         "`Triggers.putIntoGraveyardFrom(Set.of(Zone.HAND, Zone.LIBRARY), AllOf(NONTOKEN, Desert), YOU_OWN)`); it does "
         "nothing for a card that left the graveyard and came back (CR 400.7)."),
        ("- `ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect(int damage)` — "
         "Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY): the targeted opponent chooses whether the source deals "
         "`damage` to them; declining returns the triggering nontoken artifact to its owner's hand.",
         "- `ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect(int damage)` — "
         "Triggers.dies(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.YOU_OWN): the targeted opponent chooses whether the "
         "source deals `damage` to them; declining returns the triggering artifact card to its owner's hand, unless it "
         "left the graveyard and came back (CR 400.7)."),
        ("- `GainLifeEffect(1)` on `Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY)` — queues one life-gain "
         "trigger for each nonland card in the mill event (Glowing One, PIP 76).",
         "- `GainLifeEffect(1)` on `Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY)` (each, not "
         "`TriggerOptions.ONE_OR_MORE`) — triggers once for each nonland card milled (Glowing One, PIP 76)."),
        ("The effect is normally wrapped in `MayEffect(...)` on `Triggers.dies(PermanentHasCountersPredicate(ANY), "
         "ObjectRelation.ANY)`;",
         "The effect is normally wrapped in `MayEffect(...)` on `Triggers.dies(PermanentHasCountersPredicate(ANY), "
         "ObjectRelation.ANOTHER_YOU_CONTROL)`;"),
    ],
    "EFFECTS_INDEX.md": [
        ("Use inside `MayEffect(...)` on `Triggers.dies(PermanentHasCountersPredicate(ANY), ObjectRelation.ANY)` for "
         "Yuna",
         "Use inside `MayEffect(...)` on `Triggers.dies(PermanentHasCountersPredicate(ANY), "
         "ObjectRelation.ANOTHER_YOU_CONTROL)` for Yuna"),
        ("| `RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect` | `(CardPredicate filter)` | trigger effect for "
         "Desert Warfare: when a matching card enters the controller's graveyard from hand or library, or a matching "
         "permanent is sacrificed, the card is the stack entry's triggering card",
         "| `RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect` | `()` | trigger effect for Desert Warfare: when "
         "a card the trigger's filter names enters the controller's graveyard from hand or library, or such a "
         "permanent is sacrificed, the card is the stack entry's triggering card (nothing for a card that left the "
         "graveyard and came back, CR 400.7)"),
    ],
    "CARD_PATTERNS_LANDS_SPELLS.md": [
        ("Use `RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect(CardPredicate)` on "
         "`Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.ANOTHER_YOU_OWN)` and "
         "`Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)`, plus the same "
         "effect on `Triggers.sacrificed(PermanentHasSubtypePredicate(DESERT), ObjectRelation.YOU_CONTROL)`. The "
         "effect returns the stack entry's triggering card (\"that card\"); the hand/library source-zone distinction is "
         "retained.",
         "Use `RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect()` on "
         "`Triggers.putIntoGraveyardFrom(Set.of(Zone.HAND, Zone.LIBRARY), AllOf(Triggers.NONTOKEN, "
         "PermanentHasSubtypePredicate(DESERT)), ObjectRelation.YOU_OWN)` (once for each Desert card), plus the same "
         "effect on `Triggers.sacrificed(PermanentHasSubtypePredicate(DESERT), ObjectRelation.YOU_CONTROL)`. The "
         "effect returns the stack entry's triggering card (\"that card\"), in the graveyard incarnation it triggered "
         "for (CR 400.7)."),
    ],
}

STALE = [
    "RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect(CardPredicate)",
    "`(CardPredicate filter)` | trigger effect for Desert Warfare",
    "queues one life-gain trigger for each nonland card",
    "the mill collector queues one separate life-gain trigger",
    "Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY), ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamage",
]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if old not in text:
                sys.exit(f"{name}: not found: {old[:100]}")
            text = text.replace(old, new)
        path.write_text(text)
    stale = [(p.name, s) for p in DOCS.glob("*.md") for s in STALE if s in p.read_text()]
    if stale:
        sys.exit(f"stale: {stale}")


if __name__ == "__main__":
    main()
