#!/usr/bin/env python3
"""Step 14, run 14 of effect-slot-refactor.md: the agent-docs passages on Saga abilities say that a chapter takes no
TriggerOptions (an "if" in its text is an effect's ConditionalEffect, CR 714.2b / 603.4) and that a final-chapter
watcher's "this ability triggers only once each turn" is TriggerLimit.ONCE_EACH_TURN, never OncePerTurnTriggerEffect.
Exact replacements; running it again changes nothing.

Usage: python3 scripts/step14-saga-docs.py [--dry-run]
"""
import sys

EDITS = [
    ("agent-docs/EFFECTS_INDEX.md",
     "Tap and life triggers (Interface Ace, Nanoform Sentinel, Moonstone Harbinger) and creature-death triggers use",
     "Tap, life and final-chapter triggers (Interface Ace, Nanoform Sentinel, Moonstone Harbinger, Tom Bombadil) and "
     "creature-death triggers use"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md",
     "(tap and life triggers (Interface Ace, Nanoform Sentinel, Sharae of Numbing Depths, Moonstone Harbinger, MACH-1) "
     "and creature-death triggers use",
     "(tap, life and final-chapter triggers (Interface Ace, Nanoform Sentinel, Sharae of Numbing Depths, Moonstone "
     "Harbinger, MACH-1, Tom Bombadil) and creature-death triggers use"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md",
     "(tap triggers (Interface Ace, Nanoform Sentinel) and creature-death triggers use",
     "(tap and final-chapter triggers (Interface Ace, Nanoform Sentinel, Tom Bombadil) and creature-death triggers "
     "use"),
    ("agent-docs/CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md",
     "\"Whenever the final chapter ability of a Saga you control triggers / resolves\" is "
     "`Triggers.finalChapterAbilityTriggers()` / `Triggers.finalChapterAbilityResolves()` (CR 714.2e).",
     "\"Whenever the final chapter ability of a Saga you control triggers / resolves\" is "
     "`Triggers.finalChapterAbilityTriggers()` / `Triggers.finalChapterAbilityResolves()` (CR 714.2e); its \"this "
     "ability triggers only once each turn\" is `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` (Tom "
     "Bombadil), never an `OncePerTurnTriggerEffect` wrapper. A chapter ability takes no `TriggerOptions` (adding one "
     "throws): its lore-counter threshold is its only intervening \"if\" (CR 714.2b), so an \"if\" in a chapter's text "
     "(\"III — If you control a creature with power 4 or greater, draw two cards\") is checked on resolution, a "
     "`ConditionalEffect` among the chapter's effects."),
    ("agent-docs/ACTIVATED_ABILITY_GUIDE.md",
     "| `Triggers.finalChapterAbilityTriggers()` / `Triggers.finalChapterAbilityResolves()` | The final chapter ability "
     "of a Saga you control triggers / resolves (CR 714.2e) |",
     "| `Triggers.finalChapterAbilityTriggers()` / `Triggers.finalChapterAbilityResolves()` | The final chapter ability "
     "of a Saga you control triggers / resolves (CR 714.2e); \"triggers only once each turn\" is "
     "`TriggerLimit.ONCE_EACH_TURN` in its `TriggerOptions` |"),
]


def main():
    dry = "--dry-run" in sys.argv
    changed = 0
    for path, old, new in EDITS:
        src = open(path).read()
        if src.count(new) == 1 and old not in src.replace(new, ""):
            continue
        if src.count(old) != 1:
            raise SystemExit("%s: the passage isn't there once" % path)
        src = src.replace(old, new)
        changed += 1
        print(path)
        if not dry:
            open(path, "w").write(src)
    print("%d passages" % changed, file=sys.stderr)


if __name__ == "__main__":
    main()
