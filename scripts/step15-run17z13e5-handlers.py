#!/usr/bin/env python3
"""Step 15, run 17z13e5: the enter watches' "you may", "you may pay" and modal abilities go through their defaults.

`EnterTriggerCollectorService` keyed eight handlers (32 registrations) by an effect's class: the "you may" routes of
the ally, "this or another", opponent, any-creature and tokens enter watches (`handleEnterMay`, `handleTokenEnterMay`,
`handleAnyCreatureEnterMay`), the "you may pay" route (`handleEnterMayPay`, the command-zone watch's only handler), the
trigger-time modal route (`handleAllyCreatureEnterModalAtTriggerTime`), the two graveyard multi-target routes
(`handleCreatureEnterReturnToHand`, `handleCreatureEnterExileGraveyardCard`) and Portcullis's
(`handleAnyCreatureExileUntilSourceLeaves`, which judged its intervening "if" by the effect's count). The defaults were
rewritten by hand to record the entering permanent whatever the effects are and to route a modal, graveyard-target or
targeted ability (a plane's among them) to its choice; this removes the class-keyed handlers with their annotations
and Javadoc, the helper only Portcullis's used and the collector's banner comments. Idempotent.
"""
import pathlib
import re
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from step15_handler_removal import has_method, remove_method  # noqa: E402

ROOT = pathlib.Path(__file__).resolve().parent.parent
PATH = ROOT / ("magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger/"
               "EnterTriggerCollectorService.java")
HANDLERS = [
    "handleAllyCreatureEnterModalAtTriggerTime", "handleCreatureEnterReturnToHand",
    "handleCreatureEnterExileGraveyardCard", "handleEnterMay", "handleTokenEnterMay", "handleAnyCreatureEnterMay",
    "handleEnterMayPay", "handleAnyCreatureExileUntilSourceLeaves",
]
HELPERS = ["countOtherCreatures"]
BANNER = re.compile(r"\n    // ──[^\n]*\n")


def remove_any_method(text, name):
    """`remove_method` for a method of any return type."""
    signature = re.search(r"\n    (?:private |public |protected )?(?:static )?[\w<>, .?\[\]]+ " + name + r"\(", text)
    if not signature:
        return text
    marker = "\n    boolean " + name + "("
    return remove_method(text[:signature.start()] + marker + text[signature.end():], name)


def main():
    text = PATH.read_text()
    for method in HANDLERS:
        if has_method(text, method):
            text = remove_method(text, method)
    for helper in HELPERS:
        text = remove_any_method(text, helper)
    text = BANNER.sub("\n", text)
    while "\n\n\n" in text:
        text = text.replace("\n\n\n", "\n\n")
    PATH.write_text(text)
    left = [name for name in HANDLERS + HELPERS if re.search(r"\b" + name + r"\(", text)]
    if left:
        sys.exit(f"still referenced: {left}")


if __name__ == "__main__":
    main()
