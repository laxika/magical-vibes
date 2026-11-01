#!/usr/bin/env python3
"""Lists every card class that registers one of the "another object enters" EffectSlots of step 12a
(effect-slot-refactor.md), with the Oracle sentences about something entering, read from the server's card-data
cache, and the relation the wording gives (step12a-enters-classify): "another" excludes the source, "this or
another" / "[name] or another" always includes it, anything else includes it when it matches (CR 603.6a: the
newcomers are checked too). Prints a TSV on stdout.
Usage: python3 scripts/step12a-enters-scan.py > out.tsv
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
scan = __import__("step11-death-scan")

SLOTS = """ON_ALLY_CREATURE_ENTERS_BATTLEFIELD ON_ALLY_CREATURES_ENTERS_BATTLEFIELD
ON_ALLY_CREATURE_OR_ENCHANTMENT_ENTERS_BATTLEFIELD ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD
ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD ON_SELF_OR_ALLY_CREATURES_ENTERS_BATTLEFIELD
ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD ON_SELF_OR_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD
COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD
ON_SELF_OR_ALLY_ARTIFACT_ENTERS_BATTLEFIELD ON_ALLY_TOKEN_ENTERS_BATTLEFIELD ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD
ON_SELF_OR_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD
ON_ANY_CREATURE_ENTERS_BATTLEFIELD ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD
ON_ENCHANTED_PLAYER_LAND_ENTERS_BATTLEFIELD ON_ALLY_PERMANENT_ENTERS_TRANSFORMED ON_OPPONENT_LAND_ENTERS_BATTLEFIELD
ON_ALLY_LAND_ENTERS_BATTLEFIELD ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD
ON_SELF_OR_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD
ON_CREATURE_ENTERS_FROM_GRAVEYARD ON_PERMANENT_ENTERS_FROM_GRAVEYARD ON_PERMANENT_ENTERS_FROM_EXILE
ON_SELF_ENTERS_FROM_GRAVEYARD ON_ANY_PERMANENT_ENTERS_BATTLEFIELD GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD
GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD GRAVEYARD_ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD
GRAVEYARD_ON_ANY_CREATURE_ENTERS_BATTLEFIELD GRAVEYARD_ON_CREATURE_ENTERS_FROM_GRAVEYARD_OR_CAST_FROM_GRAVEYARD
GRAVEYARD_ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD GRAVEYARD_ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD
ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD""".split()
SLOT_RE = re.compile(r"EffectSlot\.(" + "|".join(SLOTS) + r")\b")
SENTENCE_RE = re.compile(r"[^.\n]*\benters?\b[^.\n]*")


def names(regs):
    """The names of the card's faces, and their short names before a comma."""
    for code, number in regs:
        p = scan.load_set(code).get(number)
        if p:
            faces = [p.get("front"), p.get("back")] + list(p.get("splitHalves") or [])
            full = [f["name"] for f in faces if f and f.get("name")]
            return full + [n.split(",")[0] for n in full]
    return []


def wording(sentence, card_names):
    """The relation family the sentence's wording gives: another, this-or-another or plain."""
    s = sentence.lower()
    selves = "|".join(["this \\w+"] + [re.escape(n.lower()) for n in card_names])
    if re.search(r"\b(%s) (or|and/or) (one or more )?(an)?other\b" % selves, s):
        return "this-or-another"
    if re.search(r"\b(an)?other\b", s):
        return "another"
    return "plain"


def main():
    print("file\tslots\ttype\twordings\tsentences")
    for root, _, files in os.walk("magical-vibes-card"):
        for name in sorted(files):
            if not name.endswith(".java"):
                continue
            path = os.path.join(root, name)
            src = open(path).read()
            slots = SLOT_RE.findall(src)
            if not slots:
                continue
            regs = scan.REG_RE.findall(src)
            text, type_line = scan.oracle(regs)
            sentences = [s.strip() for s in SENTENCE_RE.findall(text or "")]
            card_names = names(regs)
            wordings = sorted({wording(s, card_names) for s in sentences}) or ["?"]
            print("\t".join([path, ",".join(sorted(set(slots))), type_line or "?", ",".join(wordings),
                             " | ".join(sentences)]))


if __name__ == "__main__":
    main()
