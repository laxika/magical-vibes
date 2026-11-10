#!/usr/bin/env python3
"""Step 15, run 17z13e7: the class-keyed handlers of the watches that had no default, or one that recorded nothing, go.

The collect-evidence, first-investigation, foretell, tax-payment, energy, loyalty-counter, lore-counter,
counters-placed, counters-removed, mana-ability-resolved and opponent-gains-control watches were read by handlers keyed
by one effect's class each (an ability with any other effect didn't trigger), and Relic Bind's "choose one" on
"whenever enchanted permanent becomes tapped" had a mode-choice route that recorded the tapped permanent only. Each
watch's default was written by hand in `MiscTriggerCollectorService` (one shared record, `queueEventAbility`), and the
battlefield and exile cast readers collect an unfiltered "whenever [a player] casts a spell" ability as "any spell"
through the generic cast collector, so the five cast handlers are unreachable. This removes the class-keyed handlers
with their annotations and Javadoc, the four collector services left empty (`CollectEvidenceTriggerCollectorService`,
`InvestigateTriggerCollectorService`, `LoyaltyCounterTriggerCollectorService`, `SagaCounterTriggerCollectorService`:
their defaults moved to `MiscTriggerCollectorService`) and the cast collector's banner comments. Idempotent.
"""
import pathlib
import re
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from step15_handler_removal import has_method, remove_method  # noqa: E402

ROOT = pathlib.Path(__file__).resolve().parent.parent
TRIGGER = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger"
HANDLERS = {
    "MiscTriggerCollectorService.java": [
        "handleEnchantedPermanentTapModal", "handleOpponentTaxPayment", "handleManaAbilityResolutionPutCountersOnSelf",
        "handleOpponentGainsControlCreateToken", "handleEnergyGainBoostSelf", "handleForetellBoostSelf",
    ],
    "SpellCastTriggerCollectorService.java": [
        "handleCounterIfNameFoundElsewhere", "handleBoostEquippedOnSpellCast",
        "handleOpponentSpellCastPutCountersOnSource", "handleCounterUnlessPays",
        "handleRevealTopCardCreatureToBattlefield",
    ],
    "DamageTriggerCollectorService.java": ["handleCountersPlaced"],
    "PermanentCounterRemovalTriggerCollectorService.java": ["handleCountersRemoved"],
}
GONE = [
    "CollectEvidenceTriggerCollectorService.java", "InvestigateTriggerCollectorService.java",
    "LoyaltyCounterTriggerCollectorService.java", "SagaCounterTriggerCollectorService.java",
]
BANNER = re.compile(r"\n    // ──[^\n]*\n")


def main():
    left = []
    for name, methods in HANDLERS.items():
        path = TRIGGER / name
        text = path.read_text()
        for method in methods:
            if has_method(text, method):
                text = remove_method(text, method)
        text = BANNER.sub("\n", text)
        while "\n\n\n" in text:
            text = text.replace("\n\n\n", "\n\n")
        path.write_text(text)
        left += [m for m in methods if re.search(r"\b" + m + r"\(", text)]
    for name in GONE:
        (TRIGGER / name).unlink(missing_ok=True)
    if left:
        sys.exit(f"still referenced: {left}")


if __name__ == "__main__":
    main()
