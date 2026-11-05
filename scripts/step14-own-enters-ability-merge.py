#!/usr/bin/env python3
"""Effect-slot refactor step 14, run 16r: the "when this enters" abilities that are one Oracle ability become one
triggered ability (CR 603.3: each time it triggers it is one object on the stack).

The cards wrote such an ability as several abilities of one effect each:

    target(F).addTrigger(Triggers.enters(ObjectRelation.THIS), a)
            .addTrigger(Triggers.enters(ObjectRelation.THIS), b);
        -> target(F).addTrigger(Triggers.enters(ObjectRelation.THIS), a, b);

    addTrigger(Triggers.enters(ObjectRelation.THIS), a);
    addTrigger(Triggers.enters(ObjectRelation.THIS), b);
        -> addTrigger(Triggers.enters(ObjectRelation.THIS), a, b);

and EtbTriggerService pooled every plain one into one stack object, so merging them changes nothing on its own; it lets
the collector put each ability on the stack as one object.

Which abilities are one: the statements whose first argument is the family's trigger (`--family`, by default
`Triggers.enters(ObjectRelation.THIS)`; a combat trigger may be narrowed by `.withFilter(…)`), grouped by that argument
and their options (the `TriggerOptions` argument as text), leaving out those marked `TriggerMode.INDEPENDENT` (an ability
of their own) and those of a keyword's own trigger (evoke, living weapon, soulbond, exploit, champion, partner with).
A card's groups are merged when its Oracle text has at most one "when this enters" ability; a card with more is merged
only as MANUAL_GROUPS says. The effects go into the group's last statement, so a local variable an effect names is
declared by then; the others are removed. The statements of one group must have one receiver: the card, one variable,
or one chained statement (`target(F).addTrigger(…).addTrigger(…)`); a group that doesn't is reported and left alone.

Usage: python3 scripts/step14-own-enters-ability-merge.py [--dry-run] [--report] <module dir>...
"""
import difflib
import importlib.util
import os
import re
import sys

_spec = importlib.util.spec_from_file_location(
    "pilot", os.path.join(os.path.dirname(os.path.abspath(__file__)), "step14-pilot-scan.py"))
pilot = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(pilot)

OWN = "Triggers.enters(ObjectRelation.THIS)"


class Family:
    """The abilities of one trigger: its first argument, how its Oracle lines read ("whenever SUBJECT … VERB"), and the
    Oracle keywords whose own triggered ability of that trigger the card may author as a statement of its own."""

    def __init__(self, trigger, subject, verb, keyword_lines=None):
        self.name = None
        self.trigger = trigger
        self.subject = subject
        self.verb = verb
        self.keyword_lines = re.compile(keyword_lines, re.M) if keyword_lines else None


SELF = None
FAMILIES = {
    # Gift's "when this permanent enters, if its gift cost was paid" is an ability of its own (CR 702.174b).
    "enters": Family(OWN, SELF, r"enters?", r"^Gift\b"),
    # Whenever this creature (or the equipped or enchanted one) attacks; "enters or attacks" is one ability. Mobilize
    # is one: its token creation and the end-step sacrifice of those tokens.
    "attacks": Family("Triggers.attacks()", SELF, r"attacks?",
                      r"^(Myriad|Annihilator|Battle cry|Melee|Training|Dethrone|Exalted|Afflict|Bushido|"
                      r"Rampage|Flanking|Raid)\b"),
    "blocks": Family("Triggers.blocks()", SELF, r"blocks", r"^(Bushido|Flanking|Rampage)\b"),
    "becomes-blocked": Family("Triggers.becomesBlocked()", SELF, r"becomes blocked",
                              r"^(Bushido|Flanking|Rampage|Afflict)\b"),
    "creature-attacks-you-or-planeswalker": Family("Triggers.creatureAttacksYouOrYourPlaneswalker()",
                                                   r"a creature\b", r"attacks you or a planeswalker"),
    "you-attack": Family("Triggers.youAttack()", r"you\b", r"attack\b", r"^(Exalted|Melee|Battle cry)\b"),
    "creature-you-control-attacks": Family("Triggers.creatureYouControlAttacks()",
                                           r"(?:a|an|another)\b[^.]*?\byou control\b", r"attacks?",
                                           r"^(Exalted|Melee|Battle cry)\b"),
}
for _name, _family in FAMILIES.items():
    _family.name = _name
family = FAMILIES["enters"]
KEYWORD_EFFECTS = re.compile(r"\b(SacrificeSelfIfEvokedEffect|LivingWeaponEffect|Soulbond\w*Effect|ExploitEffect|"
                             r"ChampionCreatureEffect|partnerWith)\b")

# Cards whose Oracle text has two "when this enters" abilities, but whose statements of one of them are split:
# statement indexes (in source order, among the card's own enters statements) of each Oracle ability to merge.
MANUAL_GROUPS = {
    # "When this creature enters, if {G}{G} was spent to cast it, search your library for a land card, reveal it, put
    # it into your hand, then shuffle. You gain 2 life."
    ("enters", "Vibrance.java"): [[1, 2]],
    # Exalted, and "whenever a creature you control attacks alone, if it's the first combat phase of the turn, untap
    # that creature. After this phase, there is an additional combat phase."
    ("creature-you-control-attacks", "FinestHour.java"): [[1, 2]],
}

# Cards whose Oracle text has one "when this enters" ability, but whose same-options statements are separate
# abilities: none known.
KEEP = set()


def scan(src):
    """Yields (kind, index) code tokens: '(' ')' ';' '{' '}' ',' outside strings and comments."""
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if src.startswith("//", i):
            i = src.index("\n", i) if "\n" in src[i:] else n
            continue
        if src.startswith("/*", i):
            i = src.index("*/", i) + 2
            continue
        if c == '"':
            if src.startswith('"""', i):
                i = src.index('"""', i + 3) + 3
                continue
            i += 1
            while src[i] != '"':
                i += 2 if src[i] == "\\" else 1
            i += 1
            continue
        if c == "'":
            i += 1
            while src[i] != "'":
                i += 2 if src[i] == "\\" else 1
            i += 1
            continue
        if c in "();{},":
            yield c, i
        i += 1


# The receiver of an unqualified `addTrigger(…)`: the card being built. Not an identifier, so no variable shares it.
SELF_RECEIVER = "<this>"


def constructor_body(src):
    """The span of the card class's constructor body, the only place whose statements are merged: a helper method may
    add abilities to another card ("conjure a card with …") or run more than once."""
    m = re.search(r"\bpublic\s+class\s+(\w+)", src)
    if not m:
        return None
    ctor = re.search(r"\bpublic\s+" + m.group(1) + r"\s*\(\s*\)\s*\{", src)
    if not ctor:
        return None
    depth = 0
    for kind, i in scan_range(src, ctor.end() - 1, len(src)):
        if kind == "{":
            depth += 1
        elif kind == "}":
            depth -= 1
            if depth == 0:
                return ctor.end(), i
    return None


class Statement:
    """One `addTrigger(…)` call: its dot (when chained), parentheses, arguments, receiver and statement start."""


def own_statements(src):
    """The card's own enters statements: each `addTrigger(` call at paren depth 0 whose first argument is OWN."""
    tokens = list(scan(src))
    depth = 0
    stmt_start = 0
    calls = []
    open_stack = []
    for kind, i in tokens:
        if kind == "(":
            open_stack.append(i)
            depth += 1
        elif kind == ")":
            start = open_stack.pop()
            depth -= 1
            m = re.search(r"(\.\s*)?\baddTrigger\s*$", src[max(0, start - 40):start])
            if m and depth == 0:
                call = Statement()
                call.dot = max(0, start - 40) + m.start() if m.group(1) else None
                call.open = start
                call.close = i
                call.stmt_start = stmt_start
                calls.append(call)
        elif depth == 0 and kind in ";{}":
            stmt_start = i + 1
    result = []
    body = constructor_body(src)
    for call in calls:
        if body is None or not body[0] <= call.open < body[1]:
            continue
        args = split_args(src, call.open + 1, call.close)
        if not args:
            continue
        first = normalize(src[args[0][0]:args[0][1]])
        # The family's trigger, or that trigger narrowed by a filter ("a Vampire you control attacks").
        if first != family.trigger and not first.startswith(family.trigger + " .withFilter(") \
                and not first.startswith(family.trigger + ".withFilter("):
            continue
        call.trigger_text = first
        call.args = args
        texts = [normalize(src[a:b]) for a, b in args]
        call.mode = next((t for t in texts[1:] if t.startswith("TriggerMode.")), None)
        call.options = next((t for t in texts[1:] if t.startswith("TriggerOptions")), "")
        call.effects = [(a, b) for (a, b), t in zip(args[1:], texts[1:])
                        if not t.startswith("TriggerMode.") and not t.startswith("TriggerOptions")]
        call.text = normalize(src[call.open + 1:call.close])
        code_start = call.stmt_start
        while True:
            m = re.compile(r"\s*(//[^\n]*\n|/\*.*?\*/)?", re.S).match(src, code_start)
            if not m or m.end() == code_start:
                break
            code_start = m.end()
        call.code_start = code_start
        if call.dot is not None and call.dot < code_start:
            call.dot = None
        if call.dot is None:
            call.receiver = SELF_RECEIVER
        else:
            receiver = normalize(src[code_start:call.dot])
            if re.fullmatch(r"[a-z]\w*", receiver):
                call.receiver = receiver
            else:
                call.receiver = "stmt@%d" % code_start
        result.append(call)
    return result


def split_args(src, start, end):
    args, depth, arg_start = [], 0, start
    for kind, i in scan_range(src, start, end):
        if kind in "({":
            depth += 1
        elif kind in ")}":
            depth -= 1
        elif kind == "," and depth == 0:
            args.append((arg_start, i))
            arg_start = i + 1
    args.append((arg_start, end))
    return [(a + len(src[a:b]) - len(src[a:b].lstrip()), b - (len(src[a:b]) - len(src[a:b].rstrip())))
            for a, b in args if src[a:b].strip()]


def scan_range(src, start, end):
    for kind, i in scan(src[start:end]):
        yield kind, start + i


def normalize(text):
    return re.sub(r"\s+", " ", text).strip()


def oracle_ability_count(src):
    """How many of the card's Oracle lines are an ability of the family's trigger, and whether a line is a keyword
    whose own ability of that trigger the card may author on its own."""
    regs = pilot.REG.findall(src)
    text = pilot.oracle(regs) or ""
    front = text.split(" // ")[0]
    if family.subject is None:
        names = set()
        for code, number in regs:
            p = pilot.load_set(code).get(number)
            if p and p.get("front") and p["front"].get("name"):
                name = p["front"]["name"]
                names.add(name)
                names.add(name.split(",")[0])
                names.add(name.split(" ")[0])
        subjects = [r"this\b", r"it\b", r"equipped creature\b", r"enchanted creature\b"] + \
            [re.escape(n) + r"\b" for n in sorted(names, key=len, reverse=True)]
        subject = "|".join(subjects)
    else:
        subject = family.subject
    pattern = re.compile(r"\bWhen(?:ever)?\s+(?:" + subject + r")(?:[^.]*?)\b" + family.verb + r"\b")
    count = sum(1 for line in front.split("\n") if pattern.search(line))
    keyword = bool(family.keyword_lines and family.keyword_lines.search(front))
    return count, keyword


def groups_for(name, src, calls):
    candidates = [k for k, c in enumerate(calls)
                  if c.mode is None and not KEYWORD_EFFECTS.search(c.text)]
    if (family.name, name) in MANUAL_GROUPS:
        # Applied once: a group whose statements no longer share their options was merged already.
        return [g for g in MANUAL_GROUPS[(family.name, name)]
                if all(k < len(calls) and k in candidates and calls[k].options == calls[g[0]].options
                       and calls[k].trigger_text == calls[g[0]].trigger_text
                       for k in g)], None
    if name in KEEP:
        return [], None
    count, keyword = oracle_ability_count(src)
    if count > 1:
        return [], "SEPARATE(%d)" % count
    if keyword:
        return [], "KEYWORD"
    by_options = {}
    for k in candidates:
        by_options.setdefault((calls[k].trigger_text, calls[k].options), []).append(k)
    return [g for g in by_options.values() if len(g) > 1], None


def variable_name(expression, taken):
    """A local variable name for an effect expression: `new MillEffect(…)` is `mill`, `ReturnToHandEffect.target()`
    is `returnToHand`."""
    m = re.match(r"(?:new\s+)?([A-Z]\w*)", expression)
    base = m.group(1) if m else "effect"
    base = re.sub(r"Effect$", "", base) or "effect"
    base = base[0].lower() + base[1:]
    name, n = base, 2
    while name in taken or name in ("new", "var", "this", "case", "default"):
        name, n = "%s%d" % (base, n), n + 1
    taken.add(name)
    return name


def line_indent(src, position):
    return re.match(r"[ \t]*", src[src.rfind("\n", 0, position) + 1:]).group(0)


def statement_end(src, call):
    """The index just past the `;` that ends the statement of `call`."""
    depth = 0
    for kind, i in scan_range(src, call.close + 1, len(src)):
        if kind == "(":
            depth += 1
        elif kind == ")":
            depth -= 1
        elif kind == ";" and depth == 0:
            return i + 1
    raise ValueError("no statement end")


def merged_call(src, members, effects, indent):
    head = "addTrigger(" + members[0].trigger_text + (", " + members[0].options if members[0].options else "")
    one_line = indent + head + ", " + ", ".join(effects) + ");"
    if len(one_line) <= 120 and "\n" not in one_line:
        return head + ", " + ", ".join(effects) + ");"
    return head + ",\n" + ",\n".join(indent + "        " + e for e in effects) + ");"


def remove_statement(src, c):
    end = statement_end(src, c)
    start = c.code_start
    line_start = src.rfind("\n", 0, start) + 1
    if not src[line_start:start].strip():
        start = line_start
    if src[end:end + 1] == "\n":
        end += 1
    return start, end, ""


def convert_one_receiver(src, members, report, name):
    edits = []
    last = members[-1]
    moved = []
    for c in members:
        moved.extend(src[a:b] for a, b in c.effects)
    first_effect, last_effect = last.effects[0][0], last.effects[-1][1]
    if "\n" in src[last.open:last.close]:
        line_start = src.rfind("\n", 0, first_effect) + 1
        own_line = not src[line_start:first_effect].strip()
        separator = ",\n" + line_indent(src, first_effect) + ("" if own_line else "        ")
    else:
        line_start = src.rfind("\n", 0, last.open) + 1
        line_end = src.find("\n", last.close)
        one_line = src[line_start:first_effect] + ", ".join(moved) + src[last_effect:line_end]
        separator = ", "
        if len(one_line) > 120:
            continuation = "\n" + line_indent(src, last.open) + "        "
            separator = "," + continuation
            before_effects = [b for a, b in last.args if a < first_effect][-1]
            edits.append((before_effects + 1, first_effect, continuation))
    edits.append((first_effect, last_effect, separator.join(moved)))
    for c in members[:-1]:
        if c.dot is not None and c.receiver.startswith("stmt@") and src[c.close + 1:].lstrip().startswith("."):
            # A chained call followed by another call of the chain: remove `.addTrigger(…)` with its line break.
            start = c.dot
            while src[start - 1] in " \t\n":
                start -= 1
            edits.append((start, c.close + 1, ""))
        elif c.dot is not None and c.receiver.startswith("stmt@"):
            report.append("%s\tCHAIN-END-NOT-LAST" % name)
            return None
        else:
            edits.append(remove_statement(src, c))
    return edits


def convert_mixed_receivers(src, members):
    """One ability whose effects target through different groups or none: each effect a target group binds becomes a
    local variable bound to its group in place (`target(F).bindEffect(v)`), and one `addTrigger` after the last
    statement takes every effect in order."""
    taken = set(re.findall(r"\b[a-z]\w*\b", src))
    edits, inserts, effects = [], {}, []
    for c in members:
        if c.receiver == SELF_RECEIVER:
            effects.extend(src[a:b] for a, b in c.effects)
            if c is not members[-1]:
                edits.append(remove_statement(src, c))
            continue
        names = []
        indent = line_indent(src, c.code_start)
        for a, b in c.effects:
            expression = src[a:b]
            if re.fullmatch(r"[a-z]\w*", expression):
                names.append(expression)
                continue
            variable = variable_name(expression, taken)
            names.append(variable)
            inserts.setdefault(c.code_start, []).append("var %s = %s;\n%s" % (variable, expression, indent))
        effects.extend(names)
        edits.append((c.dot, c.close + 1, "".join(".bindEffect(%s)" % n for n in names)))
    last = members[-1]
    if last.receiver == SELF_RECEIVER:
        indent = line_indent(src, last.code_start)
        edits.append((last.code_start, statement_end(src, last), merged_call(src, members, effects, indent)))
    else:
        end = statement_end(src, last)
        indent = line_indent(src, last.code_start)
        inserts.setdefault(end, []).append("\n" + indent + merged_call(src, members, effects, indent))
    for position, texts in inserts.items():
        edits.append((position, position, "".join(texts)))
    return edits


def convert(name, src, report):
    calls = own_statements(src)
    if len(calls) < 2:
        return src
    groups, note = groups_for(name, src, calls)
    if note:
        report.append("%s\t%s" % (name, note))
    edits = []
    for group in groups:
        members = [calls[k] for k in group]
        if len({c.receiver for c in members}) == 1:
            group_edits = convert_one_receiver(src, members, report, name)
            if group_edits is None:
                return src
        else:
            report.append("%s\tMIXED %s" % (name, [c.receiver for c in members]))
            group_edits = convert_mixed_receivers(src, members)
        edits.extend(group_edits)
    if not edits:
        return src
    # Apply from the end; an insertion sorts after a replacement that ends where it starts.
    edits.sort(key=lambda e: (e[0], e[1]), reverse=True)
    for start, end, text in edits:
        src = src[:start] + text + src[end:]
    return src


def main():
    global family
    dry_run = "--dry-run" in sys.argv
    for arg in sys.argv[1:]:
        if arg.startswith("--family="):
            family = FAMILIES[arg.split("=", 1)[1]]
    show_report = "--report" in sys.argv
    roots = [a for a in sys.argv[1:] if not a.startswith("--")]
    report, changed = [], 0
    for root_dir in roots:
        for root, _, files in os.walk(root_dir):
            if "/build/" in root or "/test/" in root:
                continue
            for name in sorted(files):
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                if src.count(family.trigger) < 2:
                    continue
                new = convert(name, src, report)
                if new == src:
                    continue
                changed += 1
                if dry_run:
                    sys.stdout.writelines(difflib.unified_diff(src.splitlines(True), new.splitlines(True),
                                                               path, path))
                else:
                    open(path, "w").write(new)
    if show_report:
        for line in report:
            print(line, file=sys.stderr)
    print("changed %d" % changed, file=sys.stderr)


if __name__ == "__main__":
    sys.exit(main())
