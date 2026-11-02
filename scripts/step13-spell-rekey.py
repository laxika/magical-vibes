#!/usr/bin/env python3
"""Re-keys the spell, land-play and activation trigger handlers (effect-slot-refactor.md step 13, family 2).

@CollectsTrigger(value = X.class, slot = EffectSlot.S) becomes @CollectsSpellAbilityTrigger(value = X.class,
watch = SpellAbilityWatch.S) for every slot S of the family, and a @CollectsTriggers container that holds only such
handlers becomes @CollectsSpellAbilityTriggers. A container mixing the family with other slots is an error.

Usage: python3 scripts/step13-spell-rekey.py FILE [FILE ...]
"""
import re
import sys

SLOTS = """ON_SELF_MANA_ABILITY_RESOLVES ON_ANY_PLAYER_CASTS_SPELL ON_CONTROLLER_CASTS_SPELL ON_CONTROLLER_COPIES_SPELL
ON_OPPONENT_COPIES_SPELL ON_CONTROLLER_COUNTERS_SPELL ON_CONTROLLER_SPELL_COUNTERED ON_SELF_SPELL_COUNTERED_OR_FIZZLED
ON_CONTROLLER_PLAYS_LAND ON_CONTROLLER_PLAYS_ANOTHER_LAND ON_OPPONENT_PLAYS_LAND ON_OPPONENT_CASTS_SPELL
GRAVEYARD_ON_CONTROLLER_CASTS_SPELL HAND_ON_CONTROLLER_CASTS_SPELL EXILE_ON_CONTROLLER_CASTS_SPELL
GRAVEYARD_ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL ON_SELF_CAST
ON_CONTROLLER_ACTIVATES_ABILITY ON_OPPONENT_ACTIVATES_ABILITY ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY
ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY ON_CONTROLLER_ACTIVATES_ETERNALIZE_OR_EMBALM
ON_OPPONENT_ACTIVATES_NONMANA_ABILITY""".split()

FAMILY = r"EffectSlot\.(%s)\b" % "|".join(SLOTS)
ONE = re.compile(r"@CollectsTrigger\((\s*value\s*=\s*[\w.]+\.class,\s*)slot\s*=\s*%s\s*\)" % FAMILY)
CONTAINER = re.compile(r"@CollectsTriggers\(\{(.*?)\}\)", re.S)


def rekey(src, path):
    def container(m):
        body = m.group(1)
        handlers = re.findall(r"@CollectsTrigger\(", body)
        family = ONE.findall(body)
        if not family:
            return m.group(0)
        if len(family) != len(handlers):
            raise ValueError("%s: a container mixes the family with other slots" % path)
        return "@CollectsSpellAbilityTriggers({" + body + "})"

    src = CONTAINER.sub(container, src)
    src = ONE.sub(lambda m: "@CollectsSpellAbilityTrigger(%swatch = SpellAbilityWatch.%s)" % (m.group(1), m.group(2)),
                  src)
    return src


def main():
    for path in sys.argv[1:]:
        src = open(path).read()
        out = rekey(src, path)
        if out != src:
            open(path, "w").write(out)
            print("rekeyed:", path)


if __name__ == "__main__":
    main()
