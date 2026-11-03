#!/usr/bin/env python3
"""Step 14, run 4a of effect-slot-refactor.md: every engine, AI, networking, domain and test-fixture reader of a card's
own "when this enters" abilities reads them through Card.getLegacyTriggerEffects / getLegacyTriggerEffectRegistrations,
which wrap each effect in its ability's options (an intervening "if" as a ConditionalEffect, a limit as a keyed
OncePerTurnTriggerEffect), the form those readers inspected before the cards moved the wrappers into TriggerOptions.

Usage: python3 scripts/step14-own-enters-readers.py
"""
import os
import re
import sys

MODULES = ["magical-vibes-engine/src/main", "magical-vibes-engine/src/testFixtures", "magical-vibes-ai/src/main",
           "magical-vibes-networking/src/main", "magical-vibes-domain/src/main", "magical-vibes-webservice/src/main"]
READS = [(re.compile(r"\bgetTriggerEffects\(Triggers\.enters\(ObjectRelation\.THIS\)\)"),
          "getLegacyTriggerEffects(Triggers.enters(ObjectRelation.THIS))"),
         (re.compile(r"\bgetTriggerEffectRegistrations\(Triggers\.enters\(ObjectRelation\.THIS\)\)"),
          "getLegacyTriggerEffectRegistrations(Triggers.enters(ObjectRelation.THIS))")]


def main():
    for module in MODULES:
        for root, _, files in os.walk(module):
            for name in files:
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                new = src
                for pattern, replacement in READS:
                    new = pattern.sub(replacement, new)
                if new != src:
                    open(path, "w").write(new)
                    print("rewrote " + path)


if __name__ == "__main__":
    sys.exit(main())
