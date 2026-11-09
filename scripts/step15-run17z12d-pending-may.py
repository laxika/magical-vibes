#!/usr/bin/env python3
"""Step 15, run 17z12d: PendingMayAbility carries the source card's graveyard stay apart from the triggering card's.

Adds the record component `sourceGraveyardEntryVersion` after `triggeringSpellSnapshot`, keeps the old canonical
signature as a constructor recording none (-1), passes the component through every `with…` copy and adds
`withSourceGraveyardEntryVersion`.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PATH = ROOT / "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/PendingMayAbility.java"

src = PATH.read_text()
if "sourceGraveyardEntryVersion" in src:
    sys.exit("already converted")

src = src.replace(
    "        StackEntry triggeringSpellSnapshot\n) {\n",
    "        StackEntry triggeringSpellSnapshot,\n"
    "        long sourceGraveyardEntryVersion\n) {\n\n"
    "    /**\n"
    "     * Constructor recording no graveyard stay of the source card ({@code -1}). {@code sourceGraveyardEntryVersion} is\n"
    "     * the graveyard incarnation the asking ability's source card had as the ability triggered (\"this card\" of an\n"
    "     * ability that triggered from a graveyard), which the stack entry the accepted ability resolves as keeps\n"
    "     * ({@code StackEntry.setSourceGraveyardEntryVersion}); {@link #triggeringCardGraveyardEntryVersion} is the triggering\n"
    "     * card's own (CR 400.7).\n"
    "     */\n"
    "    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,\n"
    "                             UUID targetCardId, String manaCost, UUID sourcePermanentId,\n"
    "                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,\n"
    "                             UUID attackedTargetId, UUID activePlayerId, UUID choicePlayerId,\n"
    "                             Permanent sourcePermanentSnapshot, UUID sourceControllerId, UUID triggeringCardId,\n"
    "                             int eventValue, UUID triggeringPermanentId, Integer sourcePowerAtTrigger,\n"
    "                             Integer xValue, Integer triggeringPermanentToughnessAtTrigger,\n"
    "                             long triggeringCardGraveyardEntryVersion, Card heldCard, UUID grantingPermanentId,\n"
    "                             StackEntry triggeringSpellSnapshot) {\n"
    "        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,\n"
    "                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, activePlayerId,\n"
    "                choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId, eventValue,\n"
    "                triggeringPermanentId, sourcePowerAtTrigger, xValue, triggeringPermanentToughnessAtTrigger,\n"
    "                triggeringCardGraveyardEntryVersion, heldCard, grantingPermanentId, triggeringSpellSnapshot, -1L);\n"
    "    }\n",
    1)

# Every copy inside a with… method or deepCopyEntryRequests ends its argument list with the triggering spell
# (`triggeringSpellSnapshot);`, `spell == null ? null : new StackEntry(spell));`, or the deep copy's).
endings = [
    ("grantingPermanentId, triggeringSpellSnapshot);", "grantingPermanentId, triggeringSpellSnapshot, sourceGraveyardEntryVersion);"),
    ("heldCard, grantingPermanentId, triggeringSpellSnapshot);", "heldCard, grantingPermanentId, triggeringSpellSnapshot, sourceGraveyardEntryVersion);"),
    ("grantingPermanentId, spell == null ? null : new StackEntry(spell));",
     "grantingPermanentId, spell == null ? null : new StackEntry(spell), sourceGraveyardEntryVersion);"),
    ("triggeringSpellSnapshot);\n    }\n\n    /** This ability holding",
     "triggeringSpellSnapshot, sourceGraveyardEntryVersion);\n    }\n\n    /** This ability holding"),
    ("triggeringSpellSnapshot == null ? null : new StackEntry(triggeringSpellSnapshot));",
     "triggeringSpellSnapshot == null ? null : new StackEntry(triggeringSpellSnapshot),\n                sourceGraveyardEntryVersion);"),
]
for old, new in endings:
    src = src.replace(old, new)

# Lines ending a copy with `triggeringSpellSnapshot);` that the replacements above didn't reach.
src = re.sub(r"(\n\s+(?:heldCard, )?(?:grantingPermanentId|permanentId),\s*\n\s+triggeringSpellSnapshot\);)",
             lambda m: m.group(1)[:-2] + ", sourceGraveyardEntryVersion);", src)
src = re.sub(r"(heldCard, permanentId,\n\s+triggeringSpellSnapshot)\);", r"\1, sourceGraveyardEntryVersion);", src)

src = src.replace(
    "    /** This ability holding {@code card}",
    "    /**\n"
    "     * This ability with {@code version} as the graveyard incarnation its source card had as the asking ability\n"
    "     * triggered ({@code -1}: none), carried from the stack entry that asked the question.\n"
    "     */\n"
    "    public PendingMayAbility withSourceGraveyardEntryVersion(long version) {\n"
    "        return new PendingMayAbility(sourceCard, controllerId, effects, description, targetCardId, manaCost,\n"
    "                sourcePermanentId, tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId,\n"
    "                activePlayerId, choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId,\n"
    "                eventValue, triggeringPermanentId, sourcePowerAtTrigger, xValue,\n"
    "                triggeringPermanentToughnessAtTrigger, triggeringCardGraveyardEntryVersion, heldCard,\n"
    "                grantingPermanentId, triggeringSpellSnapshot, version);\n"
    "    }\n\n"
    "    /** This ability holding {@code card}",
    1)

PATH.write_text(src)
print("converted", PATH.relative_to(ROOT))
