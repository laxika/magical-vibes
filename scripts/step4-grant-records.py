"""EffectSlot refactor step 4: effect and interaction records that hold a granted ability as a slot plus
an effect (or a slot plus a list of effects) hold an Ability (or a List<Ability>) instead.

The record keeps a constructor with its old component list, which builds the ability through
LegacySlots, and accessors with the old component names, so card classes and engine readers are
unchanged. A compact constructor that reads the old components becomes the prologue of that constructor
(statements before this(...), Java 25), where the old components are its parameters.

Usage: python3 scripts/step4-grant-records.py FILE[:RecordName] ...
"""
import re
import sys

SINGLE = 'single'
LIST = 'list'


def split_top(params):
    parts, depth, cur = [], 0, ''
    for c in params:
        if c in '<(':
            depth += 1
        elif c in '>)':
            depth -= 1
        if c == ',' and depth == 0:
            parts.append(cur.strip())
            cur = ''
        else:
            cur += c
    if cur.strip():
        parts.append(cur.strip())
    return parts


def param_type_name(param):
    param = ' '.join(param.split())
    m = re.match(r'(.*\S)\s+(\w+)$', param)
    return m.group(1), m.group(2)


def find_record(s, name):
    pattern = r'(record ' + (re.escape(name) if name else r'\w+') + r')\s*\('
    m = re.search(pattern, s)
    if not m:
        return None
    start = m.end()
    depth, j = 1, start
    while depth:
        if s[j] == '(':
            depth += 1
        elif s[j] == ')':
            depth -= 1
        j += 1
    brace = s.index('{', j)
    return m, start, j - 1, brace


def body_end(s, brace):
    depth, j = 0, brace
    while True:
        if s[j] == '{':
            depth += 1
        elif s[j] == '}':
            depth -= 1
            if depth == 0:
                return j
        j += 1


def convert(path, record_name):
    s = open(path).read()
    found = find_record(s, record_name)
    if not found:
        return 'no record'
    m, pstart, pend, brace = found
    simple_name = re.search(r'record (\w+)', m.group(1)).group(1)
    params = split_top(s[pstart:pend])
    typed = [param_type_name(p) for p in params]
    pair = None
    for i in range(len(typed) - 1):
        (t1, n1), (t2, n2) = typed[i], typed[i + 1]
        if t1 == 'EffectSlot' and t2 == 'CardEffect':
            pair = (i, SINGLE)
            break
        if t1 == 'EffectSlot' and re.fullmatch(r'(java\.util\.)?List<CardEffect>', t2):
            pair = (i, LIST)
            break
    if pair is None:
        return 'no slot/effect pair'
    i, kind = pair
    slot_name, effect_name = typed[i][1], typed[i + 1][1]
    end = body_end(s, brace)
    body = s[brace + 1:end]
    prologue = ''
    compact = re.search(r'\n[ \t]*public ' + simple_name + r'\s*\{', body)
    if compact:
        cstart = compact.end()
        depth, j = 1, cstart
        while depth:
            if body[j] == '{':
                depth += 1
            elif body[j] == '}':
                depth -= 1
            j += 1
        compact_body = body[cstart:j - 1]
        if re.search(r'\b(' + slot_name + '|' + effect_name + r')\b', compact_body):
            prologue = compact_body.strip('\n')
            prologue = prologue.rstrip() + '\n' if prologue.strip() else ''
            body = body[:compact.start()] + body[j:]
    if kind == SINGLE:
        new_type, new_name = 'Ability', 'grantedAbility'
        build = f'LegacySlots.grant({slot_name}, {effect_name})'
        accessors = f'''
    /** The slot of {{@link #{new_name}}}, or {{@code null}} when there is none. */
    public EffectSlot {slot_name}() {{
        return {new_name} == null ? null : LegacySlots.slotOf({new_name});
    }}

    /** The effect of {{@link #{new_name}}}, or {{@code null}} when there is none. */
    public CardEffect {effect_name}() {{
        return {new_name} == null ? null : LegacySlots.effectsOf({new_name}).getFirst();
    }}
'''
    else:
        new_type, new_name = 'java.util.List<Ability>', 'grantedAbilities'
        build = f'LegacySlots.abilities({slot_name}, {effect_name})'
        accessors = f'''
    /** The slot of {{@link #{new_name}}}. */
    public EffectSlot {slot_name}() {{
        return LegacySlots.slotOf({new_name}.getFirst());
    }}

    /** The effects of {{@link #{new_name}}}, in order. */
    public java.util.List<CardEffect> {effect_name}() {{
        return {new_name}.stream().flatMap(ability -> LegacySlots.effectsOf(ability).stream()).toList();
    }}
'''
    old_params = s[pstart:pend]
    indent = re.search(r'\n(\s*)\S', old_params)
    new_params = [p for p in params]
    new_params[i:i + 2] = [f'{new_type} {new_name}']
    sep = ',\n' + indent.group(1) if indent else ', '
    lead = old_params[:len(old_params) - len(old_params.lstrip())]
    trail = old_params[len(old_params.rstrip()):]
    new_param_text = lead + sep.join(new_params) + trail
    args = [n for _, n in typed]
    args[i:i + 2] = [build]
    member_indent = '    ' if not s[:m.start()].rstrip().endswith('{') and m.start() - s.rfind('\n', 0, m.start()) <= 8 else '        '
    line_start = s.rfind('\n', 0, m.start()) + 1
    record_indent = s[line_start:m.start()]
    record_indent = re.match(r'\s*', record_indent).group(0)
    mi = record_indent + '    '
    signature = f'{mi}public {simple_name}('
    flat = [" ".join(p.split()) for p in params]
    if len(signature) + len(", ".join(flat)) + 3 <= 110:
        signature += ", ".join(flat) + ') {'
    else:
        signature += (',\n' + ' ' * len(signature)).join(flat) + ') {'
    compat = (f'\n\n{mi}/** The grant as the slot and effect{"s" if kind == LIST else ""} that stand for it. */\n'
              f'{signature}\n{prologue}{mi}    this({", ".join(args)});\n{mi}}}\n')
    accessors = '\n'.join((mi + line[4:]) if line.startswith('    ') else line for line in accessors.split('\n'))
    rest = body.rstrip()
    rest = rest[1:] if rest.startswith('\n\n') else rest
    new_s = (s[:pstart] + new_param_text + s[pend:brace + 1] + compat.rstrip('\n') + '\n' + rest + '\n'
             + accessors.rstrip() + '\n' + record_indent + s[end:])
    package = re.match(r'package ([\w.]+);', new_s).group(1)
    if package != 'com.github.laxika.magicalvibes.model':
        for cls in ('Ability', 'LegacySlots'):
            line = f'import com.github.laxika.magicalvibes.model.{cls};'
            if line in new_s:
                continue
            imports = list(re.finditer(r'^import [\w.]+;$', new_s, re.M))
            later = [i for i in imports if i.group(0) > line and not i.group(0).startswith('import java')]
            at = later[0].start() if later else (imports[-1].end() + 1 if imports else new_s.index('\n\n') + 2)
            new_s = new_s[:at] + line + '\n' + new_s[at:]
    new_s = re.sub(r'(\* @param\s+)' + slot_name + r'\b[^\n]*\n(\s*\*\s{3,}[^@\n][^\n]*\n)*', r'\1' + new_name + ' the granted ability\n', new_s, count=1)
    new_s = re.sub(r'\s*\* @param\s+' + effect_name + r'\b[^\n]*\n(\s*\*\s{3,}[^@\n][^\n]*\n)*', '\n', new_s, count=1)
    open(path, 'w').write(new_s)
    return None


def main(argv):
    for arg in argv:
        path, _, name = arg.partition(':')
        problem = convert(path, name or None)
        if problem:
            print(f'{arg}: {problem}')


if __name__ == '__main__':
    main(sys.argv[1:])
