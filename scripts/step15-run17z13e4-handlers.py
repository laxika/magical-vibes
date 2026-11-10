#!/usr/bin/env python3
"""Step 15, run 17z13e4: the creature-death watches have one default each that records the death.

`DeathTriggerCollectorService.registerCreatureDeath` keyed 22 handlers by an effect's class (the "you may", "you may
pay" and "you may pay life" routes of "a creature you control", "a creature or planeswalker you control", "a nontoken
creature you control" and "a creature an opponent controls" dies, the plain entries of "a creature dies" that recorded
less than its default, the delayed returns and the perpetual grant of "equipped creature dies"), and judged three
trigger conditions by the class: Lifeline's "if another creature is on the battlefield", Diabolic Servitude's "the
creature put onto the battlefield with this" and The Skullspore Nexus's "one or more" with its total power. The
defaults (`handleAnyCreatureDeathDefault`, `handleAllyNontokenDefault`, `handleOpponentCreatureDeathDefault`,
`handleEquippedCreatureDeathDefault`) were rewritten by hand to one shared record (`queueDeathAbility`); this removes
the class-keyed handlers with their annotations and Javadoc, their registrations, the helpers only they used and the
collector's banner comments. Idempotent.
"""
import pathlib
import re
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from step15_handler_removal import has_method, remove_method  # noqa: E402

ROOT = pathlib.Path(__file__).resolve().parent.parent
PATH = ROOT / ("magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger/"
               "DeathTriggerCollectorService.java")
HANDLERS = [
    "handleAllyCreatureMayPay", "handleAllyCreatureMayPayLife", "handleAllyCreatureMay",
    "handleAllyCreatureOrPlaneswalkerMay",
    "handleEquippedCreatureDelayedReturn", "handleEquippedCreatureDeathPerpetualGrant",
    "handleAnyCreatureDeathReturnTriggeringPermanent", "handleAnyCreatureDeathBoostSelf",
    "handleAnyCreatureDeathPutCounters", "handleAnyCreatureDeathPutCountersOnSelf", "handleAnyCreatureDeathUntap",
    "handleAnyCreatureDeathTap", "handleAnyCreatureDeathMay", "handleAnyCreatureDeathCreateTokenCopy",
    "handleAnyCreatureDeathDelayedReturnUnderOwnersControl", "handleAnyCreatureDeathExileLinkedCreature",
    "handleAnyCreatureDeathPutCountersOnSourceCard",
    "handleAllyNontokenCreateTokenWithTotalDyingCreaturesPower", "handleAllyNontokenMay", "handleAllyNontokenMayPay",
    "handleAllyNontokenInterveningIfConditional",
    "handleOpponentCreatureDeathMay", "handleOpponentCreatureDelayedReturnUnderControl",
    "hasAnotherCreatureAtDeath", "isFirstNontokenDeathInSimultaneousEvent", "totalPowerOfSimultaneousNontokenDeaths",
]
REGISTRATION = re.compile(r"\n        registerCreatureDeath\(CreatureDeathWatch\.\w+, \w+\.class,\s*\n?"
                          r"(?:[^;]*?)(?:this::(\w+)|->\s*(\w+)\()[^;]*;", re.S)
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
    handlers = set(HANDLERS)
    text = REGISTRATION.sub(lambda m: "" if (m.group(1) or m.group(2)) in handlers else m.group(0), text)
    for method in HANDLERS:
        text = remove_any_method(text, method)
    text = BANNER.sub("\n", text)
    while "\n\n\n" in text:
        text = text.replace("\n\n\n", "\n\n")
    PATH.write_text(text)
    left = [m for m in HANDLERS if re.search(r"\b" + m + r"\(", text)]
    print(f"DeathTriggerCollectorService.java: {len(HANDLERS) - len(left)} of {len(HANDLERS)} gone"
          + (f", left: {left}" if left else ""))


if __name__ == "__main__":
    main()
