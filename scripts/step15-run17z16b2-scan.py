#!/usr/bin/env python3
"""Step 15, run 17z16b2: which cards read X in a triggered ability (read-only).

A triggered ability whose target is chosen as it goes on the stack used to get the event's value (the damage dealt,
the spell's or the sacrificed permanent's mana value, the number of counters) as the X of its stack entry, where the
entry of an ability that chooses nothing has X 0. This scan lists every card class that writes an X reader inside a
triggered ability, by the trigger it is written on, so the cards whose X was the event's value can be told from the
ones whose X is the spell's or the cost's (CR 107.3).

An X reader is one of: the `XValue` amount, an X card or permanent predicate (`...XPredicate`), an X flag of a
graveyard effect (`requiresManaValueEqualsX`, `requiresManaValueAtMostX`, `manaValueXOffset`) or, with
`--with-handlers`, an effect whose handler reads the stack entry's X (`--handlers` lists them from the engine sources).

The statements looked at are `addTrigger(...)`, `addSimultaneousTrigger(...)`, `new TriggeredAbility(...)` and a
`target(...)...addTrigger(...)` chain, each taken whole (to its closing parenthesis). A reader the class writes
outside such a statement is listed as "elsewhere in the class": an effect built in a local variable (Riveteers
Ascendancy) is found that way, next to the X of an activated ability or of "enters with X counters", which a reader
tells apart.

Usage: python3 scripts/step15-run17z16b2-scan.py [--handlers] [--with-handlers] [--tests] [--all]

  default      the cards with an X reader in a triggered ability whose trigger is one of the event-value triggers
  --all        every trigger, not only the event-value ones
  --handlers   the effect handlers that read `entry.getXValue()` and the effect each handles
  --with-handlers  also count a card that writes one of those effects (most read X only for a filter or an amount
               the card writes, so this is the wide list to read, not the list of cards to convert)
  --tests      the card test classes of the cards printed, as Gradle arguments (filter after each module's task)
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARD_ROOT = ROOT / "magical-vibes-card"
TEST_ROOT = ROOT / "magical-vibes-card-test"
HANDLERS = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/effect"

# The triggers whose target or graveyard-card choice was given the event's value as its X: the damage, sacrifice,
# cast and counters-put families.
EVENT_VALUE_TRIGGER = re.compile(r"Damage|Prevented|^sacrificed|[cC]asts?\w*Spell|[cC]ounters?Put|PutCounters|"
                                 r"Put(?:PlusOne|MinusOne)Counters?")

X_READER = re.compile(
    r"\bXValue\b|\b\w+XPredicate\b|\b\w*XWhenMadnessOtherwisePredicate\b|requiresManaValueEqualsX|"
    r"requiresManaValueAtMostX|manaValueXOffset")
STATEMENT_START = re.compile(r"\b(?:addTrigger|addSimultaneousTrigger)\s*\(|new\s+TriggeredAbility\s*\(")
TRIGGER = re.compile(r"Triggers\.(\w+)")


def statement(source, start):
    """The call that begins at `start`, with what follows it up to the statement's end."""
    depth = 0
    i = source.index("(", start)
    while i < len(source):
        c = source[i]
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                break
        i += 1
    return source[start:i + 1]


def chain_prefix(source, start):
    """`target(...)` or `targetUpTo(...)` written before an `.addTrigger(` of the same statement."""
    begin = max(source.rfind(";", 0, start), source.rfind("{", 0, start), source.rfind("}", 0, start)) + 1
    return source[begin:start]


def handler_effects():
    readers = {}
    for path in sorted(HANDLERS.rglob("*EffectHandler.java")):
        text = path.read_text()
        if "getXValue()" not in text:
            continue
        handled = re.search(r"handledEffect\(\)\s*\{\s*return\s+(\w+)\.class", text)
        if handled:
            readers[handled.group(1)] = path.name
    return readers


def main():
    args = set(sys.argv[1:])
    readers = handler_effects()
    if "--handlers" in args:
        for effect, handler in sorted(readers.items()):
            print(f"{effect}: {handler}")
        print(f"{len(readers)} effects whose handler reads the entry's X")
        return
    effect_reader = (re.compile(r"\bnew\s+(" + "|".join(map(re.escape, readers)) + r")\b")
                     if readers and "--with-handlers" in args else None)
    found = []
    for path in sorted(CARD_ROOT.rglob("*.java")):
        if "/src/main/java/" not in str(path):
            continue
        source = path.read_text()
        hits = []
        inside = set()
        triggers_of_card = set()
        for match in STATEMENT_START.finditer(source):
            text = statement(source, match.start())
            prefix = chain_prefix(source, match.start()) if match.group(0).startswith("add") else ""
            whole = prefix + text
            triggers = set(TRIGGER.findall(whole))
            if "--all" not in args:
                triggers = {name for name in triggers if EVENT_VALUE_TRIGGER.search(name)}
            triggers_of_card |= triggers
            reads = set(X_READER.findall(whole))
            if effect_reader:
                reads |= {"handler:" + name for name in effect_reader.findall(whole)}
            if reads and triggers:
                inside |= reads
                hits.append((sorted(triggers), sorted(reads), "in the ability"))
        # A reader written outside the statement (an effect built in a local variable, Riveteers Ascendancy) is listed
        # too, marked, so the reader can tell it from an activated ability's or an "enters with X counters" X.
        elsewhere = set(X_READER.findall(source)) - inside
        if elsewhere and triggers_of_card:
            hits.append((sorted(triggers_of_card), sorted(elsewhere), "elsewhere in the class"))
        if hits:
            found.append((path, hits))
    if "--tests" in args:
        by_letter = {}
        for path, _ in found:
            name = path.stem + "Test"
            for test in TEST_ROOT.rglob(name + ".java"):
                letter = test.relative_to(TEST_ROOT).parts[0]
                fqcn = re.search(r"^package\s+([\w.]+);", test.read_text(), re.M).group(1) + "." + name
                by_letter.setdefault(letter, set()).add(fqcn)
        parts = []
        for letter in sorted(by_letter):
            parts.append(f":magical-vibes-card-test:{letter}:test")
            parts.extend(f"--tests {fqcn}" for fqcn in sorted(by_letter[letter]))
        print(" ".join(parts))
        return
    for path, hits in found:
        for triggers, reads, where in hits:
            print(f"{path.stem}: {', '.join(triggers) or '?'} | {', '.join(reads)} | {where}")
    print(f"{len(found)} cards")


if __name__ == "__main__":
    main()
