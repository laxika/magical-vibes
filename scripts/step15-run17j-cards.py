#!/usr/bin/env python3
"""Step 15, run 17j: the "you attack" family's trigger-event conditions are event conditions and trigger filters.

Run 17i's rule (CR 603.4: an intervening "if" is only an "if" that immediately follows the trigger condition) for the
"whenever you attack", "whenever you attack a player" and "whenever you attack a player or planeswalker" abilities, on
the battlefield, in a graveyard and in the command zone:

- A condition of the trigger event written as an intervening "if" is the ability's event condition: run 17i's
  CONVERSIONS (the Oracle scan's lists) applied to these factories' statements (`step15-run17i-cards.convert`).
- "Whenever one or more [filter] you control attack" / "whenever you attack with one or more [filter]"
  (`MinimumMatchingAttackers(1, filter)` on `Triggers.youAttack()`) is the trigger's filter, the documented form of
  `Triggers.youAttack()` (Vrestin, Witch-king): "that many" is then the number of matching attackers, the stack entry's
  event value, and "they" the matching attackers.
- W'Kabi, Shield of the Nation's "whenever you attack with your commander, if you control an artifact with mana value
  4 or greater" splits its compound intervening "if" into the event condition and the intervening "if".

Idempotent.

Usage: step15-run17j-cards.py <letter module dir>...
"""
import importlib.util
import pathlib
import re
import sys

spec = importlib.util.spec_from_file_location("cards17i", pathlib.Path(__file__).with_name("step15-run17i-cards.py"))
cards17i = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cards17i)

ROOT = cards17i.ROOT
YOU_ATTACK_FACTORIES = ("youAttack", "youAttackPlayer", "youAttackPlayerOrPlaneswalker")
cards17i.TRIGGER = re.compile(r"Triggers\.(" + "|".join(YOU_ATTACK_FACTORIES) + r")\(\)")

# "Whenever one or more [filter] you control attack": the Oracle scan's "you attack" abilities with
# MinimumMatchingAttackers(1, ...), Lulu's specialized faces among them.
FILTER_CARDS = set(("CelebornTheWise ChocoSeekerOfParadise ChongAndLilyNomads LuluForgetfulHollyphant PoeticIngenuity "
                    "ProdigysPrototype TheEarthKing TheUrDragon VarinaLichQueen").split())

FILTERED = re.compile(r"Triggers\.youAttack\(\),(\s*)TriggerOptions\.DEFAULT\.withInterveningIf\(\s*"
                      r"new MinimumMatchingAttackers\(1,\s*")

# "Whenever your commander attacks" from a graveyard (Endless Ranks of HYDRA, Vivi's Persistence) triggers once for each
# attacking commander its owner owns, whoever attacks with it: a "creature attacks" trigger filtered to the owner's
# commanders, which the graveyard reads don't have yet. They keep their form and the collector's fan-out (Findings).
EXCLUDED = {"EndlessRanksOfHYDRA", "VivisPersistence"}

WKABI_BEFORE = """TriggerOptions.DEFAULT.withInterveningIf(
                        new AllConditions(List.of(
                                        new HasAttacker(new PermanentIsCommanderPredicate(true)),
                                        new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                                new PermanentIsArtifactPredicate(),
                                                new PermanentMinManaValuePredicate(4))))
                                ))),"""
WKABI_AFTER = """TriggerOptions.DEFAULT
                        .withEventCondition(new HasAttacker(new PermanentIsCommanderPredicate(true)))
                        .withInterveningIf(new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentMinManaValuePredicate(4))))),"""


def to_filter(source):
    """`youAttack(), DEFAULT.withInterveningIf(new MinimumMatchingAttackers(1, P)), effects` -> `youAttack().withFilter(P), effects`."""
    converted = 0
    while True:
        match = FILTERED.search(source)
        if not match:
            return source, converted
        # The predicate runs to the MinimumMatchingAttackers call's closing parenthesis.
        call_open = source.index("new MinimumMatchingAttackers(", match.start()) + len("new MinimumMatchingAttackers")
        call_close = cards17i.closing(source, call_open)
        predicate = source[match.end():call_close].strip()
        option_open = source.index("withInterveningIf(", match.start()) + len("withInterveningIf")
        option_close = cards17i.closing(source, option_open)
        if source[call_close + 1:option_close].strip():
            raise SystemExit("unexpected text after MinimumMatchingAttackers: " + source[call_close:option_close])
        rest = source[option_close + 1:]
        comma = re.match(r"\s*,\s*", rest)
        source = (source[:match.start()] + "Triggers.youAttack().withFilter(" + predicate + ")," + match.group(1)
                  + rest[comma.end():])
        converted += 1


def drop_unused_imports(source):
    for name in ("MinimumMatchingAttackers", "TriggerOptions", "AllConditions"):
        import_line = re.compile(r"^import [\w.]+\." + name + r";\n", re.M)
        body = import_line.sub("", source)
        if not re.search(r"\b" + name + r"\b", body):
            source = body
    return source


def main(modules):
    by_card = {}
    for condition, cards in cards17i.CONVERSIONS.items():
        for card in cards.split():
            if card in EXCLUDED:
                continue
            by_card.setdefault(card, set()).add(condition)
    total = 0
    for module in modules:
        for path in sorted((pathlib.Path(module) / ROOT).glob("*/*.java")):
            source = original = path.read_text()
            converted = 0
            if path.stem in FILTER_CARDS:
                source, count = to_filter(source)
                converted += count
            if path.stem == "WKabiShieldOfTheNation" and WKABI_BEFORE in source:
                source = source.replace(WKABI_BEFORE, WKABI_AFTER)
                converted += 1
            if path.stem in by_card:
                source, count = cards17i.convert(source, by_card[path.stem], path.stem)
                converted += count
                if cards17i.convert(source, by_card[path.stem], path.stem)[1]:
                    raise SystemExit(f"{path.stem}: a listed intervening if was left in a \"you attack\" trigger")
            if source != original:
                path.write_text(drop_unused_imports(source))
                total += converted
                print(f"{path.stem}: {converted}")
    print(f"{total} abilities converted")


if __name__ == "__main__":
    main(sys.argv[1:])
