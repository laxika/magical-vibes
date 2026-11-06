#!/usr/bin/env python3
"""Step 15 run 2 of effect-slot-refactor.md: the card classes of the departed permanent's last-known information.

Usage: step15-lki-cards.py <card module dir>... (e.g. magical-vibes-card/b)

The effects read the stack entry's triggering permanent as they resolve, so the classes that only rebuilt themselves
into another effect at trigger time go:

- ``new CreateTokensForEachDyingSourceCounterEffect([type,] template)`` and
  ``new CreateTokensForEachLeavingSourceCounterEffect(type, template)`` become
  ``template.withAmount(new CountersOnTriggeringPermanent([type]))``;
- ``new ExileTopCardsForEachDyingCreatureCounterMayPlayUntilNextEndStepEffect()`` becomes
  ``new ExileTopCardsMayPlayUntilNextEndStepEffect(new CountersOnTriggeringPermanent())``;
- Sporogenesis' "whenever a creature with a fungus counter on it dies" gets its filter (the old collector skipped a
  creature without fungus counters);
- Dual Nature's "whenever a nontoken creature leaves the battlefield" gets its filter (the old collector skipped
  tokens);
- ``new MoveDyingSourceCountersToTargetCreatureEffect(Map.of(), ...)`` loses its empty counter snapshot;
- Reyhan's "is put into the command zone, if it had one or more +1/+1 counters on it" gets the filter its death
  trigger has (the old collector skipped a commander without +1/+1 counters);
- The Ooze's "a creature you control with a +1/+1 counter on it" and Donatello's "if it had counters on it" get their
  filters (the old collectors skipped a permanent without counters).
"""
import pathlib
import re
import sys

AMOUNT_IMPORT = 'import com.github.laxika.magicalvibes.model.amount.CountersOnTriggeringPermanent;'
CREATE_TOKEN_IMPORT = 'import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;'
EXILE_IMPORT = 'import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepEffect;'
GONE = ['CreateTokensForEachDyingSourceCounterEffect', 'CreateTokensForEachLeavingSourceCounterEffect',
        'ExileTopCardsForEachDyingCreatureCounterMayPlayUntilNextEndStepEffect']


def split_args(text):
    """Splits a constructor's argument text at its top-level commas."""
    args, depth, start = [], 0, 0
    for i, ch in enumerate(text):
        if ch in '([{':
            depth += 1
        elif ch in ')]}':
            depth -= 1
        elif ch == ',' and depth == 0:
            args.append(text[start:i].strip())
            start = i + 1
    if text.strip():
        args.append(text[start:].strip())
    return args


def closing_paren(s, open_index):
    depth = 0
    for i in range(open_index, len(s)):
        if s[i] == '(':
            depth += 1
        elif s[i] == ')':
            depth -= 1
            if depth == 0:
                return i
    raise ValueError('unbalanced')


def rewrite_token_counters(s, cls):
    marker = 'new ' + cls + '('
    while marker in s:
        start = s.index(marker)
        open_index = start + len(marker) - 1
        close = closing_paren(s, open_index)
        args = split_args(s[open_index + 1:close])
        if len(args) == 1:
            counter_type, template = None, args[0]
        else:
            counter_type, template = args
        amount = 'new CountersOnTriggeringPermanent(' + ('' if counter_type in (None, 'null') else counter_type) + ')'
        s = s[:start] + template + '.withAmount(' + amount + ')' + s[close + 1:]
    s = re.sub(r'\b' + cls + r'(\s+\w+\s*=)', r'CreateTokenEffect\1', s)
    return s


def wrap_long_lines(s):
    """Moves a rewritten line's ``.withAmount(...)`` to a line of its own when the line is over 120 characters."""
    lines = []
    marker = '.withAmount(new CountersOnTriggeringPermanent('
    for line in s.split('\n'):
        if len(line) > 120 and marker in line:
            head, tail = line.split(marker, 1)
            indent = len(line) - len(line.lstrip()) + 8
            lines.append(head)
            lines.append(' ' * indent + marker + tail)
        else:
            lines.append(line)
    return '\n'.join(lines)


def add_import(s, line):
    if line in s:
        return s
    imports = list(re.finditer(r'^import [^;]+;$', s, re.M))
    last = imports[-1]
    return s[:last.end()] + '\n' + line + s[last.end():]


FILTERED = ('Sporogenesis.java', 'DualNature.java', 'ReyhanLastOfTheAbzan.java', 'TheOoze.java',
            'DonatelloMutantMechanic.java')


def convert(path):
    s = path.read_text()
    original = s
    snapshot = 'new MoveDyingSourceCountersToTargetCreatureEffect(Map.of(), '
    if (not any(cls in s for cls in GONE) and snapshot not in s
            and path.name not in FILTERED):
        return False
    if snapshot in s:
        s = s.replace(snapshot, 'new MoveDyingSourceCountersToTargetCreatureEffect(')
        if not re.search(r'\bMap\.', s):
            s = re.sub(r'^import java\.util\.Map;\n', '', s, flags=re.M)
    for cls in GONE[:2]:
        if cls in s:
            s = rewrite_token_counters(s, cls)
            s = add_import(s, AMOUNT_IMPORT)
            s = add_import(s, CREATE_TOKEN_IMPORT)
    old = 'new ExileTopCardsForEachDyingCreatureCounterMayPlayUntilNextEndStepEffect()'
    if old in s:
        s = s.replace(old, 'new ExileTopCardsMayPlayUntilNextEndStepEffect(new CountersOnTriggeringPermanent())')
        s = add_import(s, AMOUNT_IMPORT)
        s = add_import(s, EXILE_IMPORT)
    if path.name == 'Sporogenesis.java':
        old = 'addTrigger(Triggers.creatureDies(ObjectRelation.ANY), saprolingsForFungusCounters);'
        if old in s:
            s = s.replace(old, 'addTrigger(Triggers.creatureDies(new PermanentHasCountersPredicate(CounterType.FUNGUS),\n'
                           '                ObjectRelation.ANY), saprolingsForFungusCounters);')
        s = add_import(s, 'import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;')
    if path.name == 'DualNature.java':
        old = ('addTrigger(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER),\n'
               '                new ExileTokensWithSameNameAsLeavingCreatureEffect());')
        if old in s:
            s = s.replace(old, 'addTrigger(Triggers.leavesBattlefield(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER),\n'
                           '                new ExileTokensWithSameNameAsLeavingCreatureEffect());')
    if path.name == 'ReyhanLastOfTheAbzan.java':
        old = 'addTrigger(Triggers.putIntoCommandZone(null, ObjectRelation.YOU_OWN),'
        if old in s:
            s = s.replace(old, 'addTrigger(Triggers.putIntoCommandZone(\n'
                           '                        new PermanentHasAtLeastCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE, 1),\n'
                           '                        ObjectRelation.YOU_OWN),')
    if path.name == 'TheOoze.java':
        old = 'addTrigger(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),'
        if old in s:
            s = s.replace(old, 'addTrigger(Triggers.leavesBattlefield(new PermanentAllOfPredicate(List.of(\n'
                               '                        Triggers.CREATURE,\n'
                               '                        new PermanentHasAtLeastCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE, 1))),\n'
                               '                        ObjectRelation.ANOTHER_YOU_CONTROL),')
            s = add_import(s, 'import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;')
            s = add_import(s, 'import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;')
    if path.name == 'DonatelloMutantMechanic.java':
        old = ('target(artifactOrCreatureYouControlTarget, 0, 1).addTrigger(Triggers.dies(\n'
               '                        Triggers.ARTIFACT,\n')
        if old in s:
            s = s.replace(old, 'target(artifactOrCreatureYouControlTarget, 0, 1).addTrigger(Triggers.dies(\n'
                               '                        new PermanentAllOfPredicate(List.of(\n'
                               '                                Triggers.ARTIFACT,\n'
                               '                                new PermanentHasCountersPredicate(CounterType.ANY))),\n')
            s = add_import(s, 'import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;')
    for cls in GONE:
        s = re.sub(r'^import com\.github\.laxika\.magicalvibes\.model\.effect\.' + cls + r';\n', '', s, flags=re.M)
    s = wrap_long_lines(s)
    if s != original:
        path.write_text(s)
        return True
    return False


def main():
    changed = []
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).rglob('*.java')):
            if convert(path):
                changed.append(str(path))
    print('\n'.join(changed))
    print(f'{len(changed)} card classes changed', file=sys.stderr)


if __name__ == '__main__':
    main()
