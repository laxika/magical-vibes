#!/usr/bin/env python3
"""Step 14, run 5 of effect-slot-refactor.md: every engine reader of a card's "at the beginning of [step or phase]"
abilities reads them through Card.getLegacyTriggerEffects / getLegacyTriggerEffectRegistrations (and
GameQueryService.getEffectiveGraveyardLegacyTriggerEffects), which wrap each effect in its ability's options (an
intervening "if" as a ConditionalEffect, "triggers only once" as an OnceOnlyTriggerEffect inside it), the form those
readers inspected before the cards moved the wrappers into TriggerOptions. PlanechaseService.trigger reads every
planar trigger that way, so a plane's intervening "if" option is checked as its own wrapper was.

Usage: python3 scripts/step14-step-readers.py
"""
import re
import sys

STEP_SERVICE = "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/turn/StepTriggerService.java"
STEP = r"Triggers\.(?:upkeep|drawStep|precombatMainPhase|beginningOfCombat|endOfCombat|postcombatMainPhase|" \
       r"secondMainPhase|endStep)\("
FILES = {
    # Every reader in the step collector but the Saga chapter one.
    STEP_SERVICE: [(re.compile(r"\bgetTriggerEffects\((?!chapter\))"), "getLegacyTriggerEffects("),
                   (re.compile(r"\bgetTriggerEffectRegistrations\("), "getLegacyTriggerEffectRegistrations("),
                   (re.compile(r"\bgetEffectiveGraveyardTriggerEffects\("),
                    "getEffectiveGraveyardLegacyTriggerEffects(")],
    "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/planar/PlanechaseService.java":
        [(re.compile(r"object\.getCard\(\)\.getTriggerEffects\(trigger\)"),
          "object.getCard().getLegacyTriggerEffects(trigger)")],
}
# A step trigger read anywhere else in the engine, the domain, the AI or networking.
OTHERS = ["magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/target/TargetLegalityService.java",
          "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/effect/normalfx/PermanentCounterSupport.java",
          "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/battlefield/PermanentRemovalService.java",
          "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/GameSetupService.java"]
for path in OTHERS:
    FILES[path] = [(re.compile(r"\bgetTriggerEffects\((\s*%s)" % STEP), r"getLegacyTriggerEffects(\1")]
PERMANENT = "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/Permanent.java"
FILES[PERMANENT] = [(re.compile(r"card\.getTriggerEffects\(yourUpkeep, Zone\.BATTLEFIELD\)"),
                     "card.getLegacyTriggerEffects(yourUpkeep, Zone.BATTLEFIELD)")]


def main():
    for path, reads in FILES.items():
        src = open(path).read()
        new = src
        count = 0
        for pattern, replacement in reads:
            new, n = pattern.subn(replacement, new)
            count += n
        if count == 0:
            raise SystemExit("no step reader in " + path)
        open(path, "w").write(new)
        print("rewrote %d readers in %s" % (count, path))


if __name__ == "__main__":
    sys.exit(main())
