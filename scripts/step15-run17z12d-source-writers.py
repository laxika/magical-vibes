#!/usr/bin/env python3
"""Step 15, run 17z12d: the collectors of graveyard abilities record their source card's graveyard stay as the
entry's source stay (`setSourceGraveyardEntryVersion`), not as a triggering card's, whatever the effects are."""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SVC = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service"


def edit(file, old, new, count=1):
    path = SVC / file
    src = path.read_text()
    if src.count(old) != count:
        sys.exit(f"{file}: expected {count} match(es) of\n{old}")
    path.write_text(src.replace(old, new))


TCS = "trigger/TriggerCollectionService.java"

# Abilities whose event is about no card: the version they recorded is the source's.
edit(TCS, """                card.getName() + "'s ability",
                new ArrayList<>(List.of(effect)));
        entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
        gameData.enqueueTrigger(entry);""",
     """                card.getName() + "'s ability",
                new ArrayList<>(List.of(effect)));
        entry.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
        gameData.enqueueTrigger(entry);""")
edit(TCS, """                entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                gameData.enqueueTrigger(entry);
""", """                entry.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                gameData.enqueueTrigger(entry);
""")
edit(TCS, """                    milledTrigger.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));""",
     """                    milledTrigger.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));""")
edit(TCS, """                // CR 400.7: the ability refers to this graveyard object, not a later one.
                triggerEntry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                gameData.stack.add(triggerEntry);""",
     """                // CR 400.7: the ability refers to this graveyard object, not a later one.
                triggerEntry.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                gameData.stack.add(triggerEntry);""")
edit(TCS, """                // CR 400.7: the ability refers to this graveyard object, not a later one.
                triggerEntry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                gameData.enqueueTrigger(triggerEntry);""",
     """                // CR 400.7: the ability refers to this graveyard object, not a later one.
                triggerEntry.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                gameData.enqueueTrigger(triggerEntry);""")
# The graveyard creature-death "you may": the source's stay, the dying card the triggering card.
edit(TCS, """            if (resolved instanceof MayEffect may) {
                gameData.queueMayAbility(card, controllerId, may, card.getId());
            } else {
                StackEntry triggerEntry = new StackEntry(""",
     """            if (resolved instanceof MayEffect may) {
                gameData.queueMayAbility(card, controllerId, may, card.getId());
                gameData.stack.getLast().setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
            } else {
                StackEntry triggerEntry = new StackEntry(""")
edit(TCS, """        gameData.stack.getLast().setTriggeringCardGraveyardEntryVersion(sourceEntryVersion);
        gameData.stack.getLast().setSourceGraveyardEntryVersion(sourceEntryVersion);""",
     """        gameData.stack.getLast().setSourceGraveyardEntryVersion(sourceEntryVersion);""")
# The cast-from-graveyard ability's triggering card is the spell; the stay it recorded is the source's.
edit(TCS, """                    entry.setTriggeringCardId(spellCard.getId());
                    entry.setTriggeringCardGraveyardEntryVersion(sourceEntryVersion);
                    entry.setSourceGraveyardEntryVersion(sourceEntryVersion);""",
     """                    entry.setTriggeringCardId(spellCard.getId());
                    entry.setSourceGraveyardEntryVersion(sourceEntryVersion);""")
# The card discarded by an opponent is both the ability's source and its triggering card.
edit(TCS, """                        selfDiscardEntry.setTriggeringCardGraveyardEntryVersion(
                                gameData.graveyardEntryVersion(discardedCard.getId()));""",
     """                        selfDiscardEntry.setTriggeringCard(discardedCard.getId(),
                                gameData.graveyardEntryVersion(discardedCard.getId()));
                        selfDiscardEntry.setSourceGraveyardEntryVersion(
                                gameData.graveyardEntryVersion(discardedCard.getId()));""")

STS = "turn/StepTriggerService.java"
edit(STS, """                        upkeepEntry.setTriggeringCardGraveyardEntryVersion(sourceEntryVersion);
                        upkeepEntry.setSourceGraveyardEntryVersion(sourceEntryVersion);""",
     """                        upkeepEntry.setSourceGraveyardEntryVersion(sourceEntryVersion);""")
edit(STS, """                delayedEntry.setTriggeringCardGraveyardEntryVersion(pending.graveyardEntryVersion());""",
     """                delayedEntry.setSourceGraveyardEntryVersion(pending.graveyardEntryVersion());""")

edit("graveyard/GraveyardService.java",
     """                trigger.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));""",
     """                trigger.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));""")
edit("combat/attack/CombatAttackService.java",
     """                    attackTrigger.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));""",
     """                    attackTrigger.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));""")
# An activated ability whose source card is in a graveyard (sacrificed as a cost) records that stay, whatever its
# effects are.
edit("ability/ActivatedAbilityExecutionService.java",
     """        if (ability.getEffects().stream().anyMatch(
                com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect.class::isInstance)
                && gameQueryService.findCardInGraveyardById(gameData, permanent.getCard().getId()) != null) {
            stackEntry.setTriggeringCardGraveyardEntryVersion(
                    gameData.graveyardEntryVersion(permanent.getCard().getId()));
        }""",
     """        if (gameQueryService.findCardInGraveyardById(gameData, permanent.getCard().getId()) != null) {
            stackEntry.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(permanent.getCard().getId()));
        }""")
edit("ability/AbilityActivationService.java",
     """        stackEntry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
        stackEntry.setTargetFilter(ability.getTargetFilter());""",
     """        stackEntry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
        stackEntry.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
        stackEntry.setTargetFilter(ability.getTargetFilter());""")
print("converted the source-stay writers")
