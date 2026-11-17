#!/usr/bin/env python3
"""Inventory of the effect durations the engine still keeps outside one Duration model.

Prints counts only. With --list FILE it also writes the classified field names to FILE, one per line, for review.

Counted:
  * GameData fields that hold the duration of an effect a resolution made (CR 611.2a), split from the turn-history
    fields ("spells cast this turn" is a record, not an effect) by name;
  * EndOfTurnStateField constants (the cleanup step's resets) by store;
  * EffectDuration / Duration constants named in main sources, cards and tests;
  * DelayedAction records and the files that name them;
  * the expire... methods of GameData and their callers outside it.

Run from the repository root: python3 -I scripts/duration-inventory.py [--list FILE]
"""
import argparse
import collections
import pathlib
import re
import subprocess

ROOT = pathlib.Path.cwd()
MODEL = ROOT / "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model"
GAME_DATA = MODEL / "GameData.java"
END_OF_TURN = MODEL / "EndOfTurnStateField.java"
DELAYED_ACTION = MODEL / "action/DelayedAction.java"

FIELD = re.compile(r"^    public (?:final |volatile |transient )*[A-Z][\w<>, ?.\[\]]* ([a-z]\w*)\s*(?:=|;)", re.M)

# A field that holds an effect's duration: named for the duration, or for a rules change an effect makes.
EFFECT = re.compile(
    r"Until|Expire|Await|NextTurn|NextUpkeep|Shields?$|Prevent|Redirect|Replacement|Doubling|Multiplication|Bonus"
    r"|Granted|Grants?$|Cant|Forbidden|Hexproof|Shroud|Protection|Silenced|Max|Fixed|Flash|Permission|WithNo|GainNo"
    r"|Skip|PendingNext|Requirement|Restriction|AsAnyColor|ManaReplacement|Keywords|StaticEffects|Overrides"
    r"|Animations|Exiling|Shuffling|Gathering|AssigningNo|LifeFloor")
HISTORY = re.compile(r"ThisTurn|Count$|Counts$|Used")

FAMILIES = collections.OrderedDict([
    ("play permissions", r"Permission|Flash|Granted|Playable|CastFrom|PlayFrom"),
    ("casting / activation restrictions", r"Cant(Cast|Play|Activate)|Silenced|MaxSpells|CantBeCountered"),
    ("damage prevention / redirection", r"Prevent|Redirect|Shields?$|LifeFloor|ProtectedFromDamage"),
    ("damage doubling / bonus", r"Doubling|Multiplication|Bonus"),
    ("player protection / keywords", r"Hexproof|Shroud|Protection|Keywords|StaticEffects"),
    ("life", r"GainLife|GainNoLife|LoseLife|LifeTotal"),
    ("combat requirements / restrictions", r"Attack|Block|Requirement|Restriction"),
    ("mana", r"Mana"),
    ("draw replacements", r"NextDraw|Draw"),
    ("zone-change replacements", r"Exiling|Shuffling|Gathering|Dying|EnterTapped|EnterCounters"),
    ("skips", r"Skip"),
    ("hand / reveal", r"Hand|Reveal"),
])


def main_sources():
    files = subprocess.run(["git", "ls-files", "*.java"], capture_output=True, text=True, check=True).stdout.split()
    return [ROOT / f for f in files]


def classify(name):
    if EFFECT.search(name):
        return "effect"
    if HISTORY.search(name):
        return "history"
    return None


def family(name):
    for label, pattern in FAMILIES.items():
        if re.search(pattern, name):
            return label
    return "other"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--list", help="write the classified field names to this file")
    args = parser.parse_args()

    game_data = GAME_DATA.read_text()
    fields = FIELD.findall(game_data)
    classified = {name: classify(name) for name in fields}
    effect_fields = sorted(n for n, c in classified.items() if c == "effect")
    history_fields = sorted(n for n, c in classified.items() if c == "history")
    print(f"GameData public fields: {len(fields)}")
    print(f"  effect durations: {len(effect_fields)}")
    print(f"  turn history (stays): {len(history_fields)}")
    by_family = collections.Counter(family(n) for n in effect_fields)
    for label, count in by_family.most_common():
        print(f"    {label}: {count}")

    end_of_turn = re.findall(r"^    ([A-Z][A-Z0-9_]+)\(Kind\.(\w+)\)", END_OF_TURN.read_text(), re.M)
    stores = collections.Counter(
        "GAME" if c.startswith("GAME_") else "PLAYER" if c.startswith("PLAYER_") else "PERMANENT"
        for c, _ in end_of_turn)
    print(f"EndOfTurnStateField constants: {len(end_of_turn)} " + ", ".join(f"{k} {v}" for k, v in stores.items()))

    sources = main_sources()
    constants = collections.Counter()
    files_by_kind = collections.Counter()
    for path in sources:
        text = path.read_text(errors="replace")
        names = re.findall(r"\b(?:EffectDuration|Duration)\.([A-Z][A-Z_]+)\b", text)
        if not names:
            continue
        kind = "test" if "/src/test/" in str(path) else "card" if "magical-vibes-card/" in str(path) else "main"
        files_by_kind[kind] += 1
        constants.update(names)
    print("Duration constants named: " + ", ".join(f"{k} files {v}" for k, v in files_by_kind.items()))
    for name, count in constants.most_common():
        print(f"    {name}: {count}")

    permits = re.findall(r"\b([A-Z]\w+)\b", DELAYED_ACTION.read_text().split("permits", 1)[1].split("{", 1)[0]) \
        if DELAYED_ACTION.exists() else []
    naming = sum(1 for p in sources if DELAYED_ACTION.exists()
                 and re.search(r"\bDelayedAction\b", p.read_text(errors="replace")))
    print(f"DelayedAction records: {len(permits)}, files naming DelayedAction: {naming}")

    expire_methods = sorted(set(re.findall(r"public [\w<>, ]+ (expire\w*)\(", game_data)))
    callers = collections.Counter()
    for path in sources:
        if path == GAME_DATA:
            continue
        text = path.read_text(errors="replace")
        for method in expire_methods:
            callers[method] += len(re.findall(r"\." + method + r"\(", text))
    print(f"GameData expire methods: {len(expire_methods)}, calls outside GameData: {sum(callers.values())}")

    if args.list:
        lines = [f"effect\t{family(n)}\t{n}" for n in effect_fields] + [f"history\t-\t{n}" for n in history_fields]
        pathlib.Path(args.list).write_text("\n".join(lines) + "\n")
        print(f"listed in {args.list}")


if __name__ == "__main__":
    main()
