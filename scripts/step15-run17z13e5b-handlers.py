#!/usr/bin/env python3
"""Step 15, run 17z13e5b: the graveyard, sacrifice, milled and exiled watches' class-keyed handlers go.

`DeathTriggerCollectorService` and `MiscTriggerCollectorService` keyed 34 handlers (36 registrations) of the
zone-change watches other than the enter ones by an effect's class: the "you may" routes, the delayed and steal
returns, the damage-to-"that artifact's / land's controller" entries, the enchanted-permanent draw, damage and
transformed-return entries, Sacred Ground's, Karmic Justice's, Jaws's, Albiorix's, Desert Warfare's, Oglor's,
Grolnok's, Athreos's, Undead Alchemist's, Lo and Li's, Glowing One's, Mirelurk Queen's and The Dark Barony's. Each
watch's default was rewritten by hand to record the event whatever the effects are (the permanent that left, the
cards put into a graveyard, the milled player and count); this removes the class-keyed handlers with their annotations
and Javadoc, the helpers only they used, the black-card watch's handlers (its default moved to the card-graveyard
defaults of `MiscTriggerCollectorService`) and the collectors' banner comments. Idempotent.
"""
import pathlib
import re
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from step15_handler_removal import has_method, remove_method  # noqa: E402

ROOT = pathlib.Path(__file__).resolve().parent.parent
TRIGGER = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger"
HANDLERS = {
    "DeathTriggerCollectorService.java": [
        "handleOwnedCreatureGraveyardDelayedReturn", "handleAnyPermanentGraveyardMay",
        "handleReturnSourceTransformedAttachedToTargetPlayer", "handleEnchantedCreatureControllerDrawsCard",
        "handleEnchantedCreatureDeathDamageEnchantedController", "handleNoncreatureArtifactSacrificedOrDestroyedDamage",
        "handleArtifactGraveyardReturnUnlessDamage", "handleArtifactGraveyardMay",
        "handleArtifactGraveyardDamageController", "handleLandGraveyardDamageController",
        "handleOpponentArtifactGraveyardMay", "handleOpponentArtifactGraveyardDamage",
        "handleBlackCardOpponentGraveyardMay", "handleBlackCardOpponentGraveyardDefault", "handleLandGraveyardReturn",
        "handleOpponentPermanentGraveyardPutCountersOnSelf", "handleNoncreaturePermanentDestroyedByOpponent",
        "handleOpponentPermanentGraveyardSteal", "handleCreaturePutIntoOwnerGraveyardMayPay",
        "handleCreaturePutIntoOwnerGraveyardCreateToken", "handleDyingCreatureReturnUnlessTargetPaysLife",
    ],
    "MiscTriggerCollectorService.java": [
        "handleOpponentNontokenPermanentSacrificeMay", "handleAnyCreatureSacrificedMay",
        "handleAnyCreatureSacrificedPutCounters", "handleExileTokenSacrificePerpetualBoost",
        "handleOpponentNontokenPermanentSacrificeSteal", "handleNonblackCardPutIntoGraveyardLoseLife",
        "handleExileTriggeringPermanentCardFromLibraryWithCroakCounter", "handleDelayedReturnMatchingCardFromGraveyard",
        "handlePerpetualTriggeredAbilityGrant", "handleExileMilledCreatureAndCreateToken",
        "handlePutCountersOnOpponentMill", "handleGainLifeOnNonlandCardsMilled", "handleNonlandCardsMilledSequence",
        "handleAnyCreatureExiledReturnTriggeringPermanent",
    ],
}
HELPERS = {
    "DeathTriggerCollectorService.java": ["addEnchantedPermanentDeathEntry", "logBlackCardOpponentGraveyard"],
    "MiscTriggerCollectorService.java": ["enqueueDelayedReturnMatchingCard"],
}
BANNER = re.compile(r"\n    // ──[^\n]*\n")


def remove_any_method(text, name):
    """`remove_method` for a method of any return type."""
    signature = re.search(r"\n    (?:private |public |protected )?(?:static )?[\w<>, .?\[\]]+ " + name + r"\(", text)
    if not signature:
        return text
    marker = "\n    boolean " + name + "("
    return remove_method(text[:signature.start()] + marker + text[signature.end():], name)


def main():
    left = []
    for name, methods in HANDLERS.items():
        path = TRIGGER / name
        text = path.read_text()
        for method in methods:
            if has_method(text, method):
                text = remove_method(text, method)
        for helper in HELPERS[name]:
            text = remove_any_method(text, helper)
        text = BANNER.sub("\n", text)
        while "\n\n\n" in text:
            text = text.replace("\n\n\n", "\n\n")
        path.write_text(text)
        left += [m for m in methods + HELPERS[name] if re.search(r"\b" + m + r"\(", text)]
    if left:
        sys.exit(f"still referenced: {left}")


if __name__ == "__main__":
    main()
