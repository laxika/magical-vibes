#!/usr/bin/env python3
"""Moves DeathTriggerCollectorService's creature-death handlers off @CollectsTrigger (effect-slot-refactor.md
step 11): each annotation naming one of the creature-death slots becomes an entry of an explicit registry keyed by
CreatureDeathWatch. Prints the registrations it made."""
import re
import sys

PATH = "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger/DeathTriggerCollectorService.java"
WATCH = {
    "ON_ALLY_CREATURE_DIES": "ALLY", "ON_ANY_CREATURE_DIES": "ANY", "ON_OPPONENT_CREATURE_DIES": "OPPONENT",
    "ON_ALLY_NONTOKEN_CREATURE_DIES": "ALLY_NONTOKEN", "ON_ANY_NONTOKEN_CREATURE_DIES": "ANY_NONTOKEN",
    "ON_ENCHANTED_PLAYER_NONTOKEN_CREATURE_DIES": "ENCHANTED_PLAYER_NONTOKEN",
    "ON_ALLY_CREATURE_OR_PLANESWALKER_DIES": "ALLY_CREATURE_OR_PLANESWALKER",
    "ON_ALLY_ARTIFACT_OR_CREATURE_DIES": "ALLY_ARTIFACT_OR_CREATURE", "ON_EQUIPPED_CREATURE_DIES": "EQUIPPED",
    "ON_DAMAGED_CREATURE_DIES": "DAMAGED", "ON_HAUNTED_CREATURE_DIES": "HAUNTED",
}
ANNOTATION = re.compile(r"[ \t]*@CollectsTrigger\(\s*value\s*=\s*([\w.]+)\.class,\s*slot\s*=\s*EffectSlot\.(\w+)\s*\)[ \t]*\n")
METHOD = re.compile(r"(?:(?:private|public|protected)\s+)?boolean\s+(\w+)\(\s*TriggerMatchContext\s+\w+,\s*([\w.<>]+)\s+\w+,")


def main():
    src = open(PATH).read()
    registrations = []
    out = []
    pos = 0
    for m in ANNOTATION.finditer(src):
        effect_class, slot = m.group(1), m.group(2)
        if slot not in WATCH:
            continue
        method = METHOD.search(src, m.end())
        if method is None:
            sys.exit("no handler method after " + m.group(0))
        registrations.append((WATCH[slot], effect_class, method.group(1), method.group(2)))
        out.append(src[pos:m.start()])
        pos = m.end()
    out.append(src[pos:])
    src = "".join(out)
    lines = ["    private void registerCreatureDeathHandlers() {"]
    for watch, effect_class, method, param in registrations:
        lines.append("        registerCreatureDeath(CreatureDeathWatch.%s, %s.class,\n"
                     "                (match, effect, context) -> %s(match, (%s) effect, context));"
                     % (watch, effect_class, method, param))
    lines.append("    }")
    marker = "    // CREATURE_DEATH_HANDLERS\n"
    if marker not in src:
        sys.exit("marker missing")
    src = src.replace(marker, "\n".join(lines) + "\n")
    open(PATH, "w").write(src)
    for r in registrations:
        print(*r)


if __name__ == "__main__":
    main()
