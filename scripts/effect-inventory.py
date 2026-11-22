#!/usr/bin/env python3
"""Inventory of the effect types in model/effect/, for effect-cleanup-refactor.md.

For every type in magical-vibes-domain's model/effect/ it finds: its kind (record, class, enum, interface), its
record components, the card classes that name it, the main-code files outside its own file and its own handlers
that name it (engine, ai, networking, webservice, websocket, application, domain, card-data, card core), its
handlers in service/effect/{normalfx,mayfx,staticfx} (a handler is a *Handler.java class whose handledEffect()
returns the type) with their line counts, and the test files that name it. "Names it" is a whole-word match of the
simple class name anywhere in the file, comments included, so a count errs towards "used".

Modes (one at a time):
  (none)                          the summary table of the plan's Measurements, recomputed
  --family PREFIX                 every effect whose name starts with PREFIX at a word boundary, most used first
  --siblings                      groups of effects whose names differ only in a subject word
  --dead                          records no card names and no main code outside their own handlers names
  --unaudited [--family PREFIX]   effects with no row in scripts/effect-audit-ledger.csv
  --tests-for CLASS... [--max-per-class N]
                                  the Gradle arguments that run the existing test classes of the cards naming
                                  those effects and of the cards constructing those (a back face is tested under
                                  its front face's name; one test task per module, each followed by its own
                                  --tests filters), plus each effect's own <Effect>Test / <Effect>HandlerTest; with
                                  --max-per-class, an evenly spaced deterministic sample of each effect's cards
  --csv FILE                      also write the per-type table to FILE

Run as: python3 -I scripts/effect-inventory.py [mode]   (from any directory)
"""
import argparse
import collections
import csv
import os
import pathlib
import re
import signal
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com/github/laxika/magicalvibes"
EFFECT_DIR = ROOT / "magical-vibes-domain/src/main/java" / PKG / "model/effect"
HANDLER_ROOT = ROOT / "magical-vibes-engine/src/main/java" / PKG / "service/effect"
HANDLER_DIRS = ("normalfx", "mayfx", "staticfx")
LEDGER = ROOT / "scripts/effect-audit-ledger.csv"
SKIPPED_DIRS = {"build", "node_modules", ".gradle", ".git", "out", "bin", "card-data-cache"}

CARD_CLASS = re.compile(r"magical-vibes-card/[^/]+/src/main/java/" + PKG + r"/cards/([a-z])/(\w+)\.java$")
IDENTIFIER = re.compile(r"\b[A-Z][A-Za-z0-9_]*\b")
DECLARATION = re.compile(
    r"^public\s+(?:(?:sealed|non-sealed|abstract|final|static)\s+)*(record|enum|interface|class|@interface)\s+(\w+)",
    re.M)
HANDLED_EFFECT = re.compile(r"handledEffect\s*\(\s*\)\s*\{\s*return\s+(?:[\w.]+\.)?(\w+)\.class\s*;")
SUBJECT_SWITCH = re.compile(r"switch\s*\(\s*[\w.]*?\.?(\w*(?:[Rr]ecipient|[Ss]cope|[Rr]eference|[Ss]ubject|[Oo]wner))\s*\(\s*\)\s*\)")
CAMEL_WORD = re.compile(r"[A-Z][a-z0-9]*|[a-z0-9]+")

# Subject phrases for --siblings, as camel-word sequences; the longest match at a position wins.
SUBJECTS = sorted({tuple(CAMEL_WORD.findall(s)) for s in [
    "Self", "Source", "SourcePermanent", "Target", "Targets", "TargetCreature", "TargetPlayer", "TargetPermanent",
    "TargetOpponent", "TargetArtifact", "TargetLand", "TargetCreatureOrPlaneswalker", "TargetPlayerOrPlaneswalker",
    "Enchanted", "EnchantedCreature", "EnchantedPermanent", "EnchantedPlayer", "EnchantedLand", "Equipped",
    "EquippedCreature", "Attached", "AttachedCreature", "Controller", "Controllers", "Owner", "Owners", "Opponent",
    "Opponents", "EachOpponent", "EachPlayer", "EachOtherPlayer", "DefendingPlayer", "TriggeringPlayer",
    "Triggering", "TriggeringPermanent", "TriggeringCreature", "Referenced", "ReferencedPermanent", "ActivePlayer",
    "Player", "Players", "DyingCreature", "DamagedPlayer", "ChosenPlayer", "Paired", "PairedCreature", "It", "Its",
    "That", "ThatPlayer", "ThatCreature", "Creature", "Creatures", "OwnCreatures", "AllCreatures", "EachCreature",
    "You", "Your",
]}, key=len, reverse=True)

# The verb families of the Measurements table: label, pattern on the effect name.
VERB_FAMILIES = [
    ("Exile", r"Exile"), ("Grant", r"Grant"), ("Return", r"Return"), ("Sacrifice", r"Sacrifice"),
    ("Destroy", r"Destroy"), ("DealDamage", r"DealDamage"), ("Double", r"Double"), ("Mill", r"Mill"),
    ("Boost", r"Boost"), ("PutCounter(s)", r"PutCounters?"), ("Draw", r"Draw"), ("Counter", r"Counter"),
    ("Discard", r"Discard"), ("Copy", r"Copy"), ("Attach", r"Attach"), ("CreateTokenCopy", r"CreateTokenCopy"),
    ("Tap / Untap", r"(?:Tap|Untap)"), ("RemoveCounter(s)", r"RemoveCounters?"), ("GainControl", r"GainControl"),
]

RARE = 2


def walk_java(base):
    for dirpath, dirnames, filenames in os.walk(base):
        dirnames[:] = [d for d in dirnames if d not in SKIPPED_DIRS]
        for name in filenames:
            if name.endswith(".java"):
                yield pathlib.Path(dirpath) / name


def rel(path):
    return path.relative_to(ROOT).as_posix()


def read(path):
    return path.read_text(encoding="utf-8", errors="replace")


def camel_words(name):
    return CAMEL_WORD.findall(name)


def family_pattern(prefix):
    return re.compile("^" + prefix + r"(?![a-z])")


def matching_close(text, open_index, opener="(", closer=")"):
    depth = 0
    for i in range(open_index, len(text)):
        if text[i] == opener:
            depth += 1
        elif text[i] == closer:
            depth -= 1
            if depth == 0:
                return i
    return len(text)


def record_components(text, name):
    m = re.search(r"\brecord\s+" + re.escape(name) + r"\s*(<[^(]*>)?\s*\(", text)
    if not m:
        return []
    start = m.end() - 1
    header = text[start + 1:matching_close(text, start)]
    header = re.sub(r"//[^\n]*|/\*.*?\*/", "", header, flags=re.S)
    header = re.sub(r"@[\w.]+(\s*\([^)]*\))?", "", header)
    parts, depth, current = [], 0, []
    for ch in header:
        if ch in "<([{":
            depth += 1
        elif ch in ">)]}":
            depth -= 1
        if ch == "," and depth == 0:
            parts.append("".join(current))
            current = []
        else:
            current.append(ch)
    parts.append("".join(current))
    components = []
    for part in (p.strip() for p in parts):
        if not part:
            continue
        type_text, _, component = part.rpartition(" ")
        components.append((re.sub(r"\s+", " ", type_text.strip()), component.strip()))
    return components


class Effect:
    def __init__(self, name, kind, path, text):
        self.name = name
        self.kind = kind
        self.path = path
        self.components = record_components(text, name) if kind == "record" else []
        self.is_effect = kind == "record" or (kind == "class" and re.search(r"\bimplements\b", text) is not None)
        self.cards = []
        self.main = collections.Counter()
        self.main_files = []
        self.handlers = []
        self.tests = []

    @property
    def card_count(self):
        return len(self.cards)

    @property
    def handler_lines(self):
        return sum(lines for _, lines in self.handlers)


class Inventory:
    def __init__(self, with_tests):
        self.effects = {}
        for path in sorted(EFFECT_DIR.glob("*.java")):
            text = read(path)
            m = DECLARATION.search(text)
            if m and m.group(2) == path.stem:
                self.effects[path.stem] = Effect(path.stem, m.group(1), path, text)
        names = set(self.effects)

        self.handler_files = []
        self.handler_of_file = {}
        for directory in HANDLER_DIRS:
            for path in sorted(walk_java(HANDLER_ROOT / directory)):
                if not path.name.endswith("Handler.java"):
                    continue
                text = read(path)
                self.handler_files.append(path)
                m = HANDLED_EFFECT.search(text)
                handled = m.group(1) if m else path.stem[:-len("Handler")]
                if handled in self.effects:
                    self.effects[handled].handlers.append((rel(path), text.count("\n")))
                    self.handler_of_file[path] = handled
        self.subject_switch_handlers = [p for p in self.handler_files if SUBJECT_SWITCH.search(read(p))]
        handler_names = {p.stem for p in self.handler_files}
        self.handler_users = collections.defaultdict(list)

        self.card_files = 0
        self.card_files_naming = collections.Counter()
        self.engine_files_naming = collections.Counter()
        for module in sorted(ROOT.glob("magical-vibes-*")):
            for path in walk_java(module):
                posix = rel(path)
                is_test = "/src/test/" in posix or "/src/testFixtures/" in posix
                if not is_test and "/src/main/" not in posix:
                    continue
                if is_test and not with_tests:
                    continue
                card = None if is_test else CARD_CLASS.search(posix)
                tokens = set(IDENTIFIER.findall(read(path)))
                found = tokens & names
                for watched in ("PermanentSubject", "PlayerSubject", "Subjects", "PermanentReference"):
                    if watched in tokens:
                        if card:
                            self.card_files_naming[watched] += 1
                        elif not is_test and posix.startswith("magical-vibes-engine/"):
                            self.engine_files_naming[watched] += 1
                if card:
                    self.card_files += 1
                    for name in found:
                        self.effects[name].cards.append((card.group(1), card.group(2)))
                elif is_test:
                    for name in found:
                        self.effects[name].tests.append(posix)
                else:
                    for handler in tokens & handler_names:
                        if handler != path.stem:
                            self.handler_users[handler].append(posix)
                    area = posix.split("/", 1)[0].removeprefix("magical-vibes-")
                    for name in found:
                        effect = self.effects[name]
                        if path == effect.path or self.handler_of_file.get(path) == name:
                            continue
                        effect.main[area] += 1
                        effect.main_files.append(posix)

    def records(self):
        return [e for e in self.effects.values() if e.kind == "record"]

    def effect_types(self):
        return [e for e in self.effects.values() if e.is_effect]


def by_use(effects):
    return sorted(effects, key=lambda e: (-e.card_count, -sum(e.main.values()), e.name))


def print_rows(effects):
    print(f"{'effect':60} {'kind':9} {'cards':>6} {'main':>5} {'tests':>5} {'hlines':>6}  components / main code")
    for e in effects:
        components = ", ".join(f"{t} {c}" for t, c in e.components) if e.kind == "record" else "-"
        main = " ".join(f"{k}:{v}" for k, v in sorted(e.main.items()))
        print(f"{e.name:60} {e.kind:9} {e.card_count:6} {sum(e.main.values()):5} {len(e.tests):5} "
              f"{e.handler_lines:6}  ({components}) {main}")


def summary(inv):
    effects = inv.effects.values()
    kinds = collections.Counter(e.kind for e in effects)
    records = inv.records()
    normalfx = [p for p in walk_java(HANDLER_ROOT / "normalfx")]
    normalfx_lines = sum(read(p).count("\n") for p in normalfx)
    once = [e for e in records if e.card_count == 1]
    once_handled = [e for e in once if e.handlers]
    twice = [e for e in records if e.card_count == 2]
    dead_none, dead_handler = dead(inv)
    groups = siblings(inv)
    nested = [e for e in records if any(t == "CardEffect" for t, _ in e.components)]
    nested_list = [e for e in records if any(re.fullmatch(r"(List|Collection|Set)<\s*(\? extends )?CardEffect\s*>", t)
                                             for t, _ in e.components)]
    effect_names = [e.name for e in inv.effect_types()]
    with_then = [n for n in effect_names if "Then" in camel_words(n)]
    with_and = [n for n in effect_names if "And" in camel_words(n)]

    def with_component(type_name):
        return [e for e in records if any(re.search(r"\b" + type_name + r"\b", t) for t, _ in e.components)]

    def naming(type_name):
        pattern = re.compile(r"\b" + type_name + r"\b")
        return [e for e in inv.effect_types() if e.name != type_name and pattern.search(read(e.path))]

    handler_tests = list(walk_java(ROOT / "magical-vibes-application/src/test"))
    rows = [
        ("Types in model/effect/", f"{len(inv.effects)} ({kinds['record']} records, {kinds['enum']} enums, "
                                    f"{kinds['interface']} interfaces, {kinds['class']} classes)"),
        ("Handler files in service/effect/normalfx", f"{len(normalfx)} files, {normalfx_lines} lines"),
        ("Records used by exactly one card", f"{len(once)} ({sum(1 for e in once if not e.components)} "
                                             f"of them have no components at all)"),
        ("... of which have a handler of their own", f"{sum(len(e.handlers) for e in once_handled)} handlers, "
                                                     f"{sum(e.handler_lines for e in once_handled)} lines"),
        ("Records used by exactly two cards", str(len(twice))),
        ("Records no card uses, referenced by nothing or only by their own handler",
         f"{len(dead_none) + len(dead_handler)} ({len(dead_none)} referenced by nothing, "
         f"{len(dead_handler)} only by their own handler)"),
        ("Groups of classes whose names differ only in the subject word",
         f"{len(groups)} groups, {sum(len(g) for g in groups.values())} classes, "
         f"{sum(len(g) - 1 for g in groups.values())} removable"),
        ("Records with a nested CardEffect component", f"{len(nested)} ({len(nested_list)} more hold a "
                                                       f"List<CardEffect>)"),
        ("Effect names containing Then / And", f"{len(with_then)} / {len(with_and)}"),
        ("Effects with a PermanentSubject / PlayerSubject component",
         f"{len(with_component('PermanentSubject'))} / {len(with_component('PlayerSubject'))} "
         f"({inv.card_files_naming['Subjects']} card files name Subjects)"),
        ("Effects with a PermanentReference component",
         f"{len(with_component('PermanentReference'))} ({inv.card_files_naming['PermanentReference']} card files)"),
        ("Handlers that switch on a recipient / scope / reference component", str(len(inv.subject_switch_handlers))),
        ("Application tests named *EffectHandlerTest",
         str(sum(1 for p in handler_tests if p.name.endswith("EffectHandlerTest.java")))),
        ("Card classes", str(inv.card_files)),
        ("EffectCountRatchetTest: records / handler classes (normalfx, mayfx, staticfx)",
         f"{kinds['record']} / {len(inv.handler_files)}"),
    ]
    width = max(len(r[0]) for r in rows)
    for what, count in rows:
        print(f"{what:{width}}  {count}")

    print()
    print(f"{'Verb':18} {'Classes':>7} {'Rare':>5} {'Cards using the family':>23}")
    for label, pattern in VERB_FAMILIES:
        regex = family_pattern(pattern)
        members = [e for e in inv.effect_types() if regex.match(e.name)]
        cards = set().union(*(set(e.cards) for e in members)) if members else set()
        rare = sum(1 for e in members if e.card_count <= RARE)
        print(f"{label:18} {len(members):7} {rare:5} {len(cards):23}")

    print()
    first_words = collections.Counter(camel_words(e.name)[0] for e in inv.effect_types() if e.card_count <= RARE)
    print(f"Rare effects (used by at most {RARE} cards) by first word: "
          + ", ".join(f"{w} {c}" for w, c in first_words.most_common(30)))
    print()
    largest = sorted(groups.items(), key=lambda kv: (-len(kv[1]), kv[0]))[:15]
    print("Largest subject-sibling groups: " + ", ".join(f"{k} ({len(v)})" for k, v in largest))


def dead(inv):
    """Records no card and no main code outside their own handlers names: (named by nothing, named only by their
    own handler). A handler that other main code names as a bean is reported by handler_used_elsewhere."""
    none, handler_only = [], []
    for e in inv.records():
        if e.cards or e.main:
            continue
        (handler_only if e.handlers else none).append(e)
    return none, handler_only


def handler_used_elsewhere(inv, effect):
    return sorted({user for handler, _ in effect.handlers for user in inv.handler_users[pathlib.Path(handler).stem]})


def subject_pattern(name):
    words = camel_words(name.removesuffix("Effect"))
    out, i, replaced = [], 0, []
    while i < len(words):
        for subject in SUBJECTS:
            if tuple(words[i:i + len(subject)]) == subject:
                if not out or out[-1] != "*":
                    out.append("*")
                replaced.append("".join(subject))
                i += len(subject)
                break
        else:
            out.append(words[i])
            i += 1
    if not replaced or all(w == "*" for w in out):
        return None
    return "".join(out)


def siblings(inv):
    groups = collections.defaultdict(list)
    for e in inv.effect_types():
        pattern = subject_pattern(e.name)
        if pattern:
            groups[pattern].append(e)
    return {k: by_use(v) for k, v in groups.items() if len(v) > 1}


def read_ledger():
    if not LEDGER.exists():
        return {}
    with LEDGER.open(newline="", encoding="utf-8") as f:
        return {row["class"]: row for row in csv.DictReader(f)}


def sample(items, n):
    if n is None or len(items) <= n:
        return items
    if n <= 0:
        return []
    if n == 1:
        return [items[0]]
    return [items[round(i * (len(items) - 1) / (n - 1))] for i in range(n)]


def card_constructors():
    """For each card class, the card classes that construct it: a back face's tests are named for its front face."""
    constructors = collections.defaultdict(set)
    for path in walk_java(ROOT / "magical-vibes-card"):
        m = CARD_CLASS.search(rel(path))
        if m:
            for built in re.findall(r"\bnew\s+([A-Z]\w*)\s*\(", read(path)):
                if built != m.group(2):
                    constructors[built].add((m.group(1), m.group(2)))
    return constructors


def has_test(letter, card):
    return (ROOT / "magical-vibes-card-test" / letter / "src/test/java" / PKG / "cards" / letter
            / f"{card}Test.java").exists()


def tests_for(inv, names, max_per_class):
    tasks = collections.defaultdict(set)
    constructors = card_constructors()
    missing = []
    for name in names:
        effect = inv.effects.get(name)
        if effect is None:
            sys.exit(f"unknown effect type: {name}")
        for own in (name + "Test", name + "HandlerTest"):
            for path in walk_java(ROOT / "magical-vibes-application/src/test"):
                if path.stem == own:
                    fqcn = rel(path).split("/src/test/java/", 1)[1].removesuffix(".java").replace("/", ".")
                    tasks[":magical-vibes-application:test"].add(fqcn)
        cards = sorted(set(effect.cards))
        with_tests = set()
        for letter, card in cards:
            tested = [(letter, card)] if has_test(letter, card) else []
            tested += [parent for parent in constructors.get(card, ()) if has_test(*parent)]
            if tested:
                with_tests.update(tested)
            else:
                missing.append(card)
        for letter, card in sample(sorted(with_tests), max_per_class):
            tasks[f":magical-vibes-card-test:{letter}:test"].add(f"com.github.laxika.magicalvibes.cards.{letter}.{card}Test")
    args = []
    for task in sorted(tasks):
        args.append(task)
        for fqcn in sorted(tasks[task]):
            args += ["--tests", fqcn]
    if missing:
        print(f"# cards without a test class: {len(missing)} ({', '.join(sorted(set(missing))[:20])}"
              f"{', ...' if len(set(missing)) > 20 else ''})", file=sys.stderr)
    print(" ".join(args))


def main():
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--siblings", action="store_true")
    mode.add_argument("--dead", action="store_true")
    mode.add_argument("--unaudited", action="store_true")
    mode.add_argument("--tests-for", nargs="+", metavar="CLASS")
    parser.add_argument("--family", metavar="PREFIX")
    parser.add_argument("--max-per-class", type=int, metavar="N")
    parser.add_argument("--csv", metavar="FILE")
    args = parser.parse_args()
    if args.max_per_class is not None and not args.tests_for:
        parser.error("--max-per-class needs --tests-for")
    if args.family and (args.siblings or args.dead or args.tests_for):
        parser.error("--family goes alone or with --unaudited")

    with_tests = bool(args.family or args.dead or args.unaudited or args.siblings or args.csv)
    inv = Inventory(with_tests=with_tests)

    if args.csv:
        with open(args.csv, "w", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            writer.writerow(["type", "kind", "cards", "main_files", "main_by_area", "handlers", "handler_lines",
                             "tests", "components"])
            for e in sorted(inv.effects.values(), key=lambda e: e.name):
                writer.writerow([e.name, e.kind, e.card_count, sum(e.main.values()),
                                 " ".join(f"{k}:{v}" for k, v in sorted(e.main.items())),
                                 " ".join(h for h, _ in e.handlers), e.handler_lines, len(e.tests),
                                 "; ".join(f"{t} {c}" for t, c in e.components)])

    if args.tests_for:
        tests_for(inv, args.tests_for, args.max_per_class)
    elif args.siblings:
        groups = siblings(inv)
        for pattern, members in sorted(groups.items(), key=lambda kv: (-len(kv[1]), kv[0])):
            print(f"{pattern} ({len(members)}): " + ", ".join(f"{e.name} [{e.card_count}]" for e in members))
        print(f"{len(groups)} groups, {sum(len(g) for g in groups.values())} classes, "
              f"{sum(len(g) - 1 for g in groups.values())} removable")
    elif args.dead:
        none, handler_only = dead(inv)
        print(f"Referenced by nothing ({len(none)}):")
        print_rows(by_use(none))
        print(f"\nReferenced only by their own handler ({len(handler_only)}):")
        print_rows(by_use(handler_only))
        used = [(e, handler_used_elsewhere(inv, e)) for e in by_use(handler_only)]
        used = [(e, users) for e, users in used if users]
        print(f"\n... of which the handler is named by other main code, so deleting it needs more than a grep "
              f"({len(used)}):")
        for e, users in used:
            print(f"{e.name}: " + ", ".join(pathlib.Path(u).stem for u in users))
    elif args.unaudited:
        ledger = read_ledger()
        effects = [e for e in inv.effect_types() if e.name not in ledger]
        if args.family:
            regex = family_pattern(args.family)
            effects = [e for e in effects if regex.match(e.name)]
        print_rows(by_use(effects))
        print(f"{len(effects)} unaudited")
    elif args.family:
        regex = family_pattern(args.family)
        effects = by_use(e for e in inv.effect_types() if regex.match(e.name))
        print_rows(effects)
        print(f"{len(effects)} effects, {sum(1 for e in effects if e.card_count <= RARE)} used by at most {RARE} cards")
    else:
        summary(inv)


if __name__ == "__main__":
    signal.signal(signal.SIGPIPE, signal.SIG_DFL)
    main()
