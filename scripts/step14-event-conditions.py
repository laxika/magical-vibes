#!/usr/bin/env python3
"""Effect-slot refactor step 14 run 16a: `ConditionalEffect.atTriggerTime` becomes the trigger's event condition.

A condition the trigger event itself requires ("whenever this creature attacks while you control a Dinosaur",
"when the fourth plan counter is put on this", "Max speed —", a station threshold) is checked only as the event
occurs (CR 603.2). The cards wrote it as `ConditionalEffect.atTriggerTime(condition, effect)` around each effect of
the ability; it becomes `TriggerOptions.withEventCondition(condition)` and the plain effects:

    addTrigger(T, ConditionalEffect.atTriggerTime(C, E))
        -> addTrigger(T, TriggerOptions.DEFAULT.withEventCondition(C), E)
    addTrigger(T, TriggerMode.M, TriggerOptions.X, ConditionalEffect.atTriggerTime(C, E))
        -> addTrigger(T, TriggerMode.M, TriggerOptions.X.withEventCondition(C), E)
    new TriggeredAbility(T, ConditionalEffect.atTriggerTime(C, E))
        -> new TriggeredAbility(T, TriggerOptions.DEFAULT.withEventCondition(C), E)

A call converts only when every effect argument is an `atTriggerTime` of the same condition. `KEEP` lists the cards
whose `atTriggerTime` is not a condition of the event (it is left alone); any other occurrence the script can't
convert (a local variable, a nested wrapper) is reported for a hand conversion. Running it again changes nothing.

Usage: step14-event-conditions.py [--dry-run] [--report]
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CARDS = os.path.join(ROOT, 'magical-vibes-card')

# "Whenever Typhoid Mary attacks, choose one at random. If you discarded a card this turn, you choose one instead":
# the "if" is the effect's own (it is judged as the ability resolves), not a condition of the attack.
KEEP = {'TyphoidMaryFractured'}

# Exact replacements for the abilities the general rewrite can't read: a local variable (Sidar Jabari), and the
# die-result conditions of "whenever you roll a blank" (Pompeii) and "whenever you roll a 4 or higher" (Mr. House),
# written as intervening-if `ConditionalEffect`s around the effect though they are conditions of the roll.
MANUAL = {
    'SidarJabariOfZhalfir': [(
        """        ConditionalEffect knightAttacker = ConditionalEffect.atTriggerTime(
                new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.KNIGHT)),
                eminenceGate);

        addTrigger(Triggers.youAttack(), knightAttacker);
        addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.COMMAND),
                TriggerOptions.DEFAULT, knightAttacker);
""",
        """        TriggerOptions withKnights = TriggerOptions.DEFAULT.withEventCondition(
                new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.KNIGHT)));

        addTrigger(Triggers.youAttack(), withKnights, eminenceGate);
        addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.COMMAND), withKnights, eminenceGate);
""")],
    'Pompeii': [(
        """        addTrigger(Triggers.youRollOneOrMoreDice(),
                new ConditionalEffect(new NotCondition(new EventValueAtLeast(1)),
                        SequenceEffect.of(new ScryEffect(2), eruptionCounter)));
""",
        """        addTrigger(Triggers.youRollOneOrMoreDice(),
                TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new EventValueAtLeast(1))),
                SequenceEffect.of(new ScryEffect(2), eruptionCounter));
""")],
    'MrHousePresidentAndCEO': [(
        """        addTrigger(Triggers.youRollDie(),
                new ConditionalEffect(
                        new EventValueAtLeast(4),
                        SequenceEffect.of(
                                ConditionalEffect.unless(
                                        new NotCondition(new EventValueAtLeast(6)),
                                        robot),
                                ConditionalEffect.unless(
                                        new EventValueAtLeast(6),
                                        robot.withSimultaneousTokens(
                                                List.of(CreateTokenEffect.ofTreasureToken(1)))))));
""",
        """        addTrigger(Triggers.youRollDie(),
                TriggerOptions.DEFAULT.withEventCondition(new EventValueAtLeast(4)),
                SequenceEffect.of(
                        ConditionalEffect.unless(
                                new NotCondition(new EventValueAtLeast(6)),
                                robot),
                        ConditionalEffect.unless(
                                new EventValueAtLeast(6),
                                robot.withSimultaneousTokens(
                                        List.of(CreateTokenEffect.ofTreasureToken(1))))));
""")],
}

CALL = re.compile(r'(\baddTrigger|\bnew\s+TriggeredAbility)\s*\(')
AT = 'ConditionalEffect.atTriggerTime('
IMPORT = 'import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;'


def split_args(s, i):
    depth = 0
    spans = []
    start = i
    j = i
    in_str = None
    while j < len(s):
        ch = s[j]
        if in_str:
            if ch == '\\':
                j += 2
                continue
            if ch == in_str:
                in_str = None
        elif ch in '"\'':
            in_str = ch
        elif ch in '([{':
            depth += 1
        elif ch in ')]}':
            if depth == 0 and ch == ')':
                if s[start:j].strip():
                    spans.append((start, j))
                return spans, j
            depth -= 1
        elif ch == ',' and depth == 0:
            spans.append((start, j))
            start = j + 1
        j += 1
    return None, None


def strip_span(s, span):
    a, b = span
    while a < b and s[a].isspace():
        a += 1
    while b > a and s[b - 1].isspace():
        b -= 1
    return a, b


def convert(s):
    """Returns (new source, converted call count)."""
    count = 0
    pos = 0
    while True:
        m = CALL.search(s, pos)
        if not m:
            return s, count
        spans, end = split_args(s, m.end())
        pos = m.end()
        if spans is None:
            continue
        args = [strip_span(s, span) for span in spans]
        texts = [s[a:b] for a, b in args]
        effect_start = 1
        option_index = None
        for index, text in enumerate(texts[1:], start=1):
            if text.startswith('TriggerMode.'):
                effect_start = index + 1
            elif text.startswith('TriggerOptions.') or text.startswith('TriggerOptions\n'):
                effect_start = index + 1
                option_index = index
        effects = list(range(effect_start, len(texts)))
        if not effects or not all(texts[i].startswith(AT) for i in effects):
            continue
        parts = []
        for i in effects:
            a, b = args[i]
            inner, inner_end = split_args(s, a + len(AT))
            if inner is None or len(inner) != 2 or inner_end != b - 1:
                parts = None
                break
            parts.append(inner)
        if not parts or len({s[slice(*strip_span(s, inner[0]))] for inner in parts}) != 1:
            continue
        condition_span = parts[0][0]
        condition = s[slice(*strip_span(s, condition_span))]
        replacements = []
        for index, (i, inner) in enumerate(zip(effects, parts)):
            a, b = args[i]
            effect_start = strip_span(s, inner[1])[0]
            if index == 0 and option_index is None:
                # "ConditionalEffect.atTriggerTime(C, E)" -> "TriggerOptions.DEFAULT.withEventCondition(C), E",
                # keeping the layout between the condition and the effect
                replacements.append((b - 1, b, ''))
                replacements.append((inner[0][1], inner[0][1], ')'))
                replacements.append((a, a + len(AT), 'TriggerOptions.DEFAULT.withEventCondition('))
            else:
                replacements.append((b - 1, b, ''))
                replacements.append((a, effect_start, ''))
        if option_index is not None:
            a, b = args[option_index]
            replacements.append((b, b, '.withEventCondition(' + condition + ')'))
        for a, b, text in sorted(replacements, key=lambda r: (r[0], r[1]), reverse=True):
            s = s[:a] + text + s[b:]
        count += 1
        pos = m.start()




def add_import(s):
    if IMPORT in s or 'TriggerOptions.DEFAULT.withEventCondition' not in s:
        return s
    anchor = 'import com.github.laxika.magicalvibes.model.trigger.Triggers;'
    if anchor in s:
        return s.replace(anchor, IMPORT + '\n' + anchor, 1)
    first_import = s.index('import ')
    return s[:first_import] + IMPORT + '\n' + s[first_import:]


def drop_unused_conditional_import(s):
    imp = 'import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;\n'
    if imp in s and 'ConditionalEffect' not in s.replace(imp, ''):
        s = s.replace(imp, '')
    return s


def main():
    dry = '--dry-run' in sys.argv
    report = '--report' in sys.argv
    changed = 0
    left = []
    for dirpath, dirnames, files in os.walk(CARDS):
        dirnames[:] = [d for d in dirnames if d != 'build']
        for f in files:
            if not f.endswith('.java'):
                continue
            path = os.path.join(dirpath, f)
            s = open(path).read()
            name = f[:-5]
            new = s
            for old, replacement in MANUAL.get(name, []):
                new = new.replace(old, replacement)
            if AT not in new and new == s or name in KEEP:
                continue
            new, n = convert(new)
            new = drop_unused_conditional_import(add_import(new))
            if AT in new:
                left.append(name)
            if new != s:
                changed += 1
                if report or dry:
                    print(('would convert ' if dry else 'converted ') + name + f' ({n})')
                if not dry:
                    open(path, 'w').write(new)
    for name in sorted(left):
        print('LEFT', name)
    print(f'{changed} card classes; KEEP: {", ".join(sorted(KEEP))}')


if __name__ == '__main__':
    main()
