#!/usr/bin/env python3
"""Step 15, run 17i: scan the attack-trigger abilities' conditions and classify them by the Oracle text.

For every triggered-ability statement of a card class whose trigger is an attack trigger, its option conditions
(IF: intervening "if", EV: event condition) and its effects' `ConditionalEffect` conditions; then each intervening "if"
classified by the card's Oracle text (card-data-cache): "IF" when every attack-trigger line of the Oracle text has an
", if" right after its trigger condition (CR 603.4), "EVENT" when none has, "MIXED" otherwise. The card's own names
are masked so "Wowzer, the Aspirational attacks, if" reads as one trigger condition. Read-only; prints the lists that
`step15-run17i-cards.py` takes.

Usage: step15-run17i-scan.py
"""
import collections
import functools
import json
import pathlib
import re

CACHE = pathlib.Path("card-data-cache")
FACTORIES = ("attacks creatureYouControlAttacks creatureAttacks creatureAttacksYouOrYourPlaneswalker creatureAttacksYou "
             "creatureAttacksWhileYouDefend youAttack youAttackPlayer youAttackPlayerOrPlaneswalker playerAttacks "
             "creaturesAttackYou opponentAttacksYourPlaneswalker attacksUnblocked enchantedCreatureAttacksUnblocked "
             "creatureYouControlAttacksUnblocked creaturesAttackYouUnblocked").split()


@functools.lru_cache(None)
def set_data(code):
    path = CACHE / f"set-{code.lower()}.json"
    if not path.exists():
        return {}
    return {printing["collectorNumber"]: printing for printing in json.load(open(path))["printings"]}


def oracle_named(java_path):
    source = pathlib.Path(java_path).read_text()
    for match in re.finditer(r'@CardRegistration\(set = "([A-Z0-9]+)", collectorNumber = "([^"]+)"', source):
        printing = set_data(match.group(1)).get(match.group(2))
        if not printing:
            continue
        texts = []
        for face in (printing.get("front"), printing.get("back")):
            if not face or not face.get("text"):
                continue
            text, name = face["text"], face.get("name") or ""
            for part in sorted({name, name.split(",")[0]}, key=len, reverse=True):
                if part:
                    text = text.replace(part, "~")
            texts.append(text)
        return "\n".join(texts)
    return None


def closing(source, index):
    depth = 0
    for j in range(index, len(source)):
        if source[j] == "(":
            depth += 1
        elif source[j] == ")":
            depth -= 1
            if depth == 0:
                return j
    return len(source)


def condition_class(expression, variables):
    expression = expression.strip()
    match = re.match(r"new (\w+)\(", expression)
    if match:
        return match.group(1)
    if expression in variables:
        return variables[expression]
    match = re.match(r"(\w+)\.", expression)
    return match.group(1) if match else expression[:30]


def abilities():
    rows = []
    for path in sorted(pathlib.Path("magical-vibes-card").glob("*/src/main/java/**/*.java")):
        source = path.read_text()
        if not any("Triggers." + factory + "(" in source for factory in FACTORIES):
            continue
        variables = {m.group(1): m.group(2) for m in re.finditer(r"\b(\w+)\s*=\s*new (\w+)\(", source)}
        for match in re.finditer(r"(addTrigger|new TriggeredAbility)\(", source):
            statement = source[match.start():closing(source, match.end() - 1) + 1]
            trigger = [f for f in FACTORIES if re.search(r"Triggers\." + f + r"\(", statement[:200])]
            if not trigger:
                continue
            options = []
            for option in re.finditer(r"with(InterveningIf|EventCondition)\(", statement):
                argument = statement[option.end():closing(statement, option.end() - 1)]
                kind = "IF" if option.group(1) == "InterveningIf" else "EV"
                options.append(kind + ":" + condition_class(argument, variables))
            effects = [condition_class(statement[c.end():closing(statement, c.end() - 1)].split(",")[0], variables)
                       for c in re.finditer(r"new ConditionalEffect\(", statement)]
            if options or effects:
                rows.append((path, trigger[0], options, effects))
    return rows


def main():
    trigger_line = re.compile(r"\b(When|Whenever)\b[^.]*?\battack", re.I)
    intervening = re.compile(r"\b(When|Whenever)\b[^,.]*?\battack[^,.]*?, if\b", re.I)
    result = collections.defaultdict(lambda: collections.defaultdict(set))
    for path, trigger, options, _ in abilities():
        ifs = [o[3:] for o in options if o.startswith("IF:")]
        if not ifs:
            continue
        lines = [line for line in re.split(r"\n|•", oracle_named(path) or "") if trigger_line.search(line)]
        count = sum(bool(intervening.search(line)) for line in lines)
        kind = "IF" if lines and count == len(lines) else "EVENT" if lines and count == 0 else "MIXED"
        for condition in ifs:
            result[(kind, trigger)][condition].add(path.stem)
    for (kind, trigger), conditions in sorted(result.items()):
        print(f"## {kind} {trigger}")
        for condition, cards in sorted(conditions.items()):
            print(f"   {condition}: {' '.join(sorted(cards))}")


if __name__ == "__main__":
    main()
