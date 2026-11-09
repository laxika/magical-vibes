#!/usr/bin/env python3
"""Step 15, run 17z13b: the emblems' "at the beginning of [step]" wrappers become emblem `TriggeredAbility`s.

`EmblemStepTriggerEffect(EmblemTriggerStep, effects, reminderText)` held an emblem's step ability as a marker in its
static-effect list, read by class by `StepTriggerService`. Each one becomes a `TriggeredAbility` on the emblem
(`CreateEmblemEffect`'s `triggeredAbilities`), read by the emblem step reader:

    UPKEEP              Triggers.upkeep(ActivePlayer.YOU)
    OPPONENT_UPKEEP     Triggers.upkeep(ActivePlayer.OPPONENT)
    DRAW_STEP           Triggers.drawStep(ActivePlayer.YOU)
    PRECOMBAT_MAIN      Triggers.precombatMainPhase(ActivePlayer.YOU)
    BEGINNING_OF_COMBAT Triggers.beginningOfCombat(ActivePlayer.YOU)
    END_STEP            Triggers.endStep(ActivePlayer.YOU)
    EACH_END_STEP       Triggers.endStep(ActivePlayer.ANY)
    END_OF_FIRST_COMBAT Triggers.endOfCombat() with the event condition
                        AllOf(ControllerTurn, FirstCombatPhase): "at the end of the first combat phase on your turn"

The wrapper's effects become the ability's effects, unchanged. A `CreateEmblemEffect` whose static list held only the
wrapper and no recipient or activated ability becomes `CreateEmblemEffect(TriggeredAbility, reminderText)`; any other
one takes the five-component form, its other static effects, recipient and activated abilities kept.

Teferi, Who Slows the Sunset's "You draw a card during each opponent's draw step" is no triggered ability (no player
gets priority between the active player's draw and the emblem controller's, its ruling): the wrapper becomes the
static `DrawDuringEachOpponentsDrawStepEffect(1)`, read by the draw step.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17z13b-step-emblems.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
T = "model.trigger."
C = "model.condition."

WRAPPER = "new EmblemStepTriggerEffect("

TRIGGERS = {
    "UPKEEP": ("Triggers.upkeep(ActivePlayer.YOU)", None),
    "OPPONENT_UPKEEP": ("Triggers.upkeep(ActivePlayer.OPPONENT)", None),
    "DRAW_STEP": ("Triggers.drawStep(ActivePlayer.YOU)", None),
    "PRECOMBAT_MAIN": ("Triggers.precombatMainPhase(ActivePlayer.YOU)", None),
    "BEGINNING_OF_COMBAT": ("Triggers.beginningOfCombat(ActivePlayer.YOU)", None),
    "END_STEP": ("Triggers.endStep(ActivePlayer.YOU)", None),
    "EACH_END_STEP": ("Triggers.endStep(ActivePlayer.ANY)", None),
    "END_OF_FIRST_COMBAT": ("Triggers.endOfCombat()",
                            "TriggerOptions.DEFAULT.withEventCondition(\n"
                            "new AllOf(List.of(new ControllerTurn(), new FirstCombatPhase())))"),
}

TEFERI_OLD = """                                new EmblemStepTriggerEffect(
                                        EmblemTriggerStep.OPPONENT_DRAW_STEP,
                                        List.of(new DrawCardEffect(1)),
                                        "You draw a card during each opponent's draw step."
                                )"""
TEFERI_NEW = """                                new DrawDuringEachOpponentsDrawStepEffect(1)"""

GONE = ["EmblemStepTriggerEffect", "EmblemTriggerStep"]


def import_line(fqcn):
    return f"import {PKG}.{fqcn};"


def add_import(text, fqcn):
    line = import_line(fqcn)
    if line in text:
        return text
    imports = list(re.finditer(r"^import [^;]+;$", text, re.M))
    pos = imports[0].start()
    return text[:pos] + line + "\n" + text[pos:]


def remove_unused_import(text, fqcn):
    line = import_line(fqcn) + "\n"
    simple = line.rsplit(".", 1)[1].rstrip(";\n")
    if line in text and not re.search(r"\b" + simple + r"\b", text.replace(line, "")):
        text = text.replace(line, "")
    return text


def sort_imports(text):
    """Keeps the project's import block sorted: the package's imports first, then java.*."""
    imports = list(re.finditer(r"^import [^;]+;$", text, re.M))
    start, end = imports[0].start(), imports[-1].end()
    block = text[start:end]
    if re.search(r"^(?!import |\s*$).+", block, re.M):
        return text
    lines = [m.group(0) for m in imports]
    ours = sorted(l for l in lines if not l.startswith("import java"))
    java = sorted(l for l in lines if l.startswith("import java"))
    joined = "\n".join(ours) + ("\n\n" + "\n".join(java) if java and ours else "\n".join(java))
    return text[:start] + joined + text[end:]


def close_paren(text, open_index):
    """The index of the parenthesis closing the one at {@code open_index}, skipping string literals."""
    depth, i = 0, open_index
    while i < len(text):
        ch = text[i]
        if ch == '"':
            i += 1
            while text[i] != '"':
                i += 2 if text[i] == "\\" else 1
        elif ch in "([{":
            depth += 1
        elif ch in ")]}":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    raise ValueError("unbalanced")


def split_args(text, open_index):
    """The top-level arguments of the call whose parenthesis opens at {@code open_index}, with their offsets."""
    end = close_paren(text, open_index)
    args, depth, start, i = [], 0, open_index + 1, open_index + 1
    while i < end:
        ch = text[i]
        if ch == '"':
            i += 1
            while text[i] != '"':
                i += 2 if text[i] == "\\" else 1
        elif ch in "([{":
            depth += 1
        elif ch in ")]}":
            depth -= 1
        elif ch == "," and depth == 0:
            args.append((start, i))
            start = i + 1
        i += 1
    if text[start:end].strip():
        args.append((start, end))
    return [(s + len(text[s:e]) - len(text[s:e].lstrip()), e - (len(text[s:e]) - len(text[s:e].rstrip())))
            for s, e in args], end


def line_indent(text, index):
    line_start = text.rfind("\n", 0, index) + 1
    line = text[line_start:index]
    return len(line) - len(line.lstrip(" "))


def reindent(text, start, end, new_indent):
    """The expression text[start:end], its continuation lines moved to sit under {@code new_indent}."""
    expr = text[start:end]
    shift = new_indent - line_indent(text, start)
    lines = expr.split("\n")
    moved = [lines[0]]
    for line in lines[1:]:
        if shift >= 0:
            moved.append(" " * shift + line)
        else:
            cut = min(-shift, len(line) - len(line.lstrip(" ")))
            moved.append(line[cut:])
    return "\n".join(moved)


def list_elements(text, start, end):
    """The elements of a {@code List.of(...)} expression at text[start:end]."""
    expr = text[start:end]
    if not expr.startswith("List.of("):
        raise ValueError(f"not a List.of: {expr[:40]}")
    args, _ = split_args(text, start + len("List.of"))
    return args


def ability_text(text, wrapper_start, indent):
    """The TriggeredAbility written for the wrapper call at {@code wrapper_start}, its first line at {@code indent}."""
    args, _ = split_args(text, wrapper_start + len(WRAPPER) - 1)
    step = text[args[0][0]:args[0][1]].replace("EmblemTriggerStep.", "")
    trigger, options = TRIGGERS[step]
    effects = [reindent(text, s, e, indent + 8) for s, e in list_elements(text, *args[1])]
    head = "new TriggeredAbility(" + trigger + ","
    if options:
        first, second = options.split("\n")
        head += "\n" + " " * (indent + 8) + first + "\n" + " " * (indent + 16) + second + ","
    body = (",\n").join(" " * (indent + 8) + effect for effect in effects)
    return head + "\n" + body + ")", step


def convert_emblem(text, emblem_start):
    """Rewrites the CreateEmblemEffect call at {@code emblem_start}; returns the new text."""
    open_index = emblem_start + len("new CreateEmblemEffect")
    args, end = split_args(text, open_index)
    indent = line_indent(text, emblem_start) + 8
    statics = list_elements(text, *args[0])
    others, abilities = [], []
    for s, e in statics:
        if text.startswith(WRAPPER, s):
            ability, _ = ability_text(text, s, indent + 8)
            abilities.append(ability)
        else:
            others.append(reindent(text, s, e, indent + 8))
    rest = [reindent(text, s, e, indent) for s, e in args[1:]]
    if not others and len(rest) == 1 and len(abilities) == 1:
        ability = ability_text(text, statics[0][0], indent)[0]
        new_args = [ability, rest[0]]
    else:
        recipient = rest[1] if len(rest) > 1 else "EmblemRecipient.CONTROLLER"
        activated = rest[2] if len(rest) > 2 else "List.of()"
        if len(rest) > 3:
            raise ValueError("an emblem with triggered abilities already")
        static_list = ("List.of()" if not others else
                       "List.of(\n" + ",\n".join(" " * (indent + 8) + o for o in others) + ")")
        ability_list = "List.of(\n" + ",\n".join(" " * (indent + 8) + a for a in abilities) + ")"
        new_args = [static_list, rest[0], recipient, activated, ability_list]
    call = "new CreateEmblemEffect(\n" + ",\n".join(" " * indent + a for a in new_args) + ")"
    return text[:emblem_start] + call + text[end + 1:]


def convert_file(path, name):
    text = path.read_text()
    if WRAPPER not in text:
        return
    original = text
    if name == "TeferiWhoSlowsTheSunset":
        if TEFERI_OLD not in text:
            raise SystemExit(f"{name}: expected text not found")
        text = text.replace(TEFERI_OLD, TEFERI_NEW)
        text = add_import(text, E + "DrawDuringEachOpponentsDrawStepEffect")
    while WRAPPER in text:
        wrapper = text.index(WRAPPER)
        emblem_start = text.rindex("new CreateEmblemEffect(", 0, wrapper)
        text = convert_emblem(text, emblem_start)
    if text == original:
        return
    if "new TriggeredAbility(" in text:
        for fqcn in ["model.TriggeredAbility", T + "Triggers", T + "ActivePlayer"]:
            text = add_import(text, fqcn)
    if "EmblemRecipient." in text:
        text = add_import(text, E + "EmblemRecipient")
    if "new FirstCombatPhase()" in text:
        for fqcn in [T + "TriggerOptions", C + "AllOf", C + "ControllerTurn", C + "FirstCombatPhase"]:
            text = add_import(text, fqcn)
    for fqcn in [E + "EmblemStepTriggerEffect", E + "EmblemTriggerStep", E + "DrawCardEffect", E + "EmblemRecipient",
                 T + "ActivePlayer"]:
        text = remove_unused_import(text, fqcn)
    text = sort_imports(text)
    for gone in GONE:
        if re.search(r"\b" + gone + r"\b", text):
            raise SystemExit(f"{name}: still names {gone}")
    path.write_text(text)
    print(f"converted {name}")


def main():
    modules = sorted(set("abcdefghijklmnopqrstuvwxyz")) + ["shared"] if sys.argv[1:] == ["all"] else sys.argv[1:]
    if not modules:
        raise SystemExit(__doc__)
    for module in modules:
        root = ROOT / f"magical-vibes-card/{module}/src/main/java"
        if not root.exists():
            continue
        for path in sorted(root.rglob("*.java")):
            convert_file(path, path.stem)


if __name__ == "__main__":
    main()
