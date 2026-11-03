#!/usr/bin/env python3
"""Step 14, run 7 of effect-slot-refactor.md: rewrites the agent-docs and skill text that tells card authors to write
the combat abilities' trigger-only wrappers, now that they are the trigger's filter and the ability's TriggerOptions:

- `AllyCombatDamageTriggerEffect(p, x[, bind[, once[, opponents]]])` in a code span becomes the filter, the options and
  the effect (`boundToDealer(x)` / `opponentsDealtDamage(x)` for the event bindings that stay);
- `BatchedCombatDamageToYouTriggerEffect(x)` becomes `TriggerOptions.ONE_OR_MORE` and the effect;
- `TriggeringPermanentConditionalEffect(p, x)` / `TriggeringCardConditionalEffect(c, x)` on a line about a combat
  trigger becomes the trigger's `.withFilter(p)` and the effect;
- `TriggerMode.PER_BLOCKER` / `ONCE_PER_BLOCK` (and the bare "(PER_BLOCKER)") become `TriggerOptions.EACH_BLOCKER` /
  `TriggerOptions.ONE_OR_MORE`;
- a rewritten filter is put on the trigger the text names just before it, else written `trigger.withFilter(…)`;
- the class entries of the wrappers say what to author instead (EXACT).

Usage: python3 scripts/step14-combat-docs.py
"""
import importlib.util
import os
import re
import sys

_here = os.path.dirname(__file__)
spec = importlib.util.spec_from_file_location("pilot_options", os.path.join(_here, "step14-pilot-options.py"))
po = importlib.util.module_from_spec(spec)
spec.loader.exec_module(po)

FILES = [os.path.join("agent-docs", f) for f in sorted(os.listdir("agent-docs")) if f.endswith(".md")
         and f != "ARCHITECTURE.md"] + [".claude/skills/implement-card/SKILL.md", ".claude/skills/review-card/SKILL.md"]
COMBAT_LINE = re.compile(r"Triggers\.(attacks|blocks|becomesBlocked|creatureYouControlAttacks|creatureAttacks|"
                         r"creatureBlocks|creatureYouControlBecomesBlocked|creatureBecomesBlocked|youAttack|"
                         r"attacksUnblocked|creatureYouControlAttacksUnblocked|dealsCombatDamage\w*|"
                         r"creatureDealsCombatDamage\w*|creatureYouControlDealsCombatDamage\w*|"
                         r"equippedCreatureDealsCombatDamage\w*)\(")

DONT_AUTHOR_ALLY = ("**Don't author the trigger-level fields** (step 14 of effect-slot-refactor.md): the creature "
                    "filter is the trigger's (`Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(p)`), "
                    "\"one or more\" is `TriggerOptions.ONE_OR_MORE`, and a plain effect is the usual form (the legacy "
                    "view wraps it in this record for the collector). Author the record only for how the effect reads "
                    "the event: `AllyCombatDamageTriggerEffect.boundToDealer(effect)` (\"put a counter on it / that "
                    "creature\") or `.opponentsDealtDamage(effect)` (\"whenever your opponents are dealt combat "
                    "damage\").")
EXACT = {
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("- `BatchedCombatDamageToYouTriggerEffect(CardEffect)` — trigger-slot marker for an "
         "`Triggers.creatureDealsCombatDamageToYou()` ability that should trigger once for a combat-damage step "
         "containing one or more qualifying creatures; the trigger collector unwraps it before resolution.",
         "- `BatchedCombatDamageToYouTriggerEffect(CardEffect)` — **don't author**: \"whenever one or more creatures "
         "deal combat damage to you\" is `addTrigger(Triggers.creatureDealsCombatDamageToYou(), "
         "TriggerOptions.ONE_OR_MORE, effect)`; the legacy view gives the collector this record."),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("| `BatchedCombatDamageToYouTriggerEffect` | `(CardEffect wrapped)` | trigger-slot marker for a "
         "combat-damage-to-you ability that queues once per combat-damage step containing one or more qualifying "
         "creatures; the collector unwraps the child effect before resolution |",
         "| `BatchedCombatDamageToYouTriggerEffect` | `(CardEffect wrapped)` | **Don't author** (step 14 of "
         "effect-slot-refactor.md): \"whenever one or more creatures deal combat damage to you\" is "
         "`addTrigger(Triggers.creatureDealsCombatDamageToYou(), TriggerOptions.ONE_OR_MORE, effect)`; the legacy "
         "view gives the collector this record |"),
    ],
}


def rewrite_calls(text, name, build):
    """Rewrites each `name(args)` call in `text` with build(args) (None leaves it). Inside a code span the result is
    code; outside one it is put in a span."""
    out, pos = [], 0
    for m in re.finditer(r"(?:new )?" + re.escape(name) + r"\(", text):
        if m.start() < pos:
            continue
        try:
            end = po.close_paren(text, m.end())
        except (IndexError, AttributeError):
            continue
        args = po.split_args(text[m.end():end - 1])
        new = build(args)
        if new is None:
            continue
        if text[:m.start()].count("`") % 2 == 0:
            new = "`" + new + "`"
        out.append(text[pos:m.start()] + new)
        pos = end
    out.append(text[pos:])
    return "".join(out)


def ally(args):
    if len(args) < 2 or any(a not in ("true", "false") for a in args[2:]) or args[0].startswith("..."):
        return None
    flags = [a == "true" for a in args[2:]] + [False] * (5 - len(args))
    bind, once, opponents = flags
    parts = []
    if args[0] not in ("null", ""):
        parts.append("trigger.withFilter(%s)" % args[0])
    if once:
        parts.append("TriggerOptions.ONE_OR_MORE")
    effect = args[1]
    if bind:
        effect = "AllyCombatDamageTriggerEffect.boundToDealer(%s)" % effect
    elif opponents:
        effect = "AllyCombatDamageTriggerEffect.opponentsDealtDamage(%s)" % effect
    return ", ".join(parts + [effect])


def batched(args):
    return "TriggerOptions.ONE_OR_MORE, %s" % args[0] if len(args) == 1 else None


CARD_SHORTHAND = [(r"\bCardSubtypePredicate\(", "PermanentHasSubtypePredicate("),
                  (r"\bCardColorPredicate\(", "PermanentColorInPredicate("),
                  (r"\bCardNotPredicate\(", "PermanentNotPredicate("),
                  (r"\bCardIsTokenPredicate\b", "PermanentIsTokenPredicate"),
                  (r"\bCardAnyOfPredicate\(", "PermanentAnyOfPredicate("),
                  (r"\bCardSupertypePredicate\(", "PermanentHasSupertypePredicate(")]


def filtered(args):
    if len(args) != 2 or args[0].startswith("..."):
        return None
    predicate = args[0]
    for pattern, rep in CARD_SHORTHAND:
        predicate = re.sub(pattern, rep, predicate)
    return "trigger.withFilter(%s), %s" % (predicate, args[1])


def attach(line):
    """Puts a rewritten filter on the trigger written just before it: "Triggers.x() trigger.withFilter(" →
    "Triggers.x().withFilter("."""
    return re.sub(r"(Triggers\.\w+\(\))`?(?: \+ | with | |`? \+ `?|`? `?)`?trigger\.withFilter\(", r"\1.withFilter(", line)


# A class's own entry ("- `TriggeringPermanentConditionalEffect(…)` — …", "| `AllyCombatDamageTriggerEffect` | …") is
# rewritten by EXACT, not as an authoring row; nor is a row of a card the conversion KEPT.
ENTRY = re.compile(r"^(?:- `|\| `)(AllyCombatDamageTriggerEffect|BatchedCombatDamageToYouTriggerEffect|"
                   r"TriggeringPermanentConditionalEffect|TriggeringCardConditionalEffect)\b")
KEPT = re.compile(r"TriggeringPermanentControllerConditionalEffect|Spined Sliver|SpinedSliver|Berserk Murlodont|"
                  r"BerserkMurlodont|Tattered Ratter|TatteredRatter|Senu|PiratedCopy|Pirated Copy|Tidus")


FILTER_NOTE = ("On a combat trigger, don't author it (step 14 of effect-slot-refactor.md): the restriction on the "
               "creature is the trigger's filter, `Triggers.creatureYouControlAttacks().withFilter(p)`, "
               "`Triggers.becomesBlocked().withFilter(p)` with `TriggerOptions.EACH_BLOCKER`; the legacy view gives "
               "the collectors this wrapper. ")
NOTES = {"AllyCombatDamageTriggerEffect": DONT_AUTHOR_ALLY + " ",
         "BatchedCombatDamageToYouTriggerEffect": None,
         "TriggeringPermanentConditionalEffect": FILTER_NOTE,
         "TriggeringCardConditionalEffect": FILTER_NOTE}


def entry_note(line, name):
    """The class entry with its "don't author" note in front of its description."""
    note = NOTES[name]
    if note is None or "step 14 of effect-slot-refactor.md" in line:
        return line
    if line.startswith("| "):
        cells = line.split(" | ")
        if len(cells) < 3:
            return line
        cells[2] = note + cells[2]
        return " | ".join(cells)
    head, sep, tail = line.partition(" — ")
    return head + sep + note + tail if sep else line


def rewrite_line(line):
    original = line
    entry = ENTRY.match(line)
    if entry:
        line = entry_note(line, entry.group(1))
        line = line.replace("TriggerMode.PER_BLOCKER", "TriggerOptions.EACH_BLOCKER")
        return line
    if KEPT.search(line):
        return line
    if "AllyCombatDamageTriggerEffect(" in line:
        line = rewrite_calls(line, "AllyCombatDamageTriggerEffect", ally)
    if "BatchedCombatDamageToYouTriggerEffect(" in line:
        line = rewrite_calls(line, "BatchedCombatDamageToYouTriggerEffect", batched)
    if COMBAT_LINE.search(line):
        for name in ("TriggeringPermanentConditionalEffect", "TriggeringCardConditionalEffect"):
            if name + "(" in line:
                line = rewrite_calls(line, name, filtered)
    line = line.replace("TriggerMode.PER_BLOCKER", "TriggerOptions.EACH_BLOCKER")
    line = line.replace("TriggerMode.ONCE_PER_BLOCK", "TriggerOptions.ONE_OR_MORE")
    line = re.sub(r"\(PER_BLOCKER\)", "(`TriggerOptions.EACH_BLOCKER`)", line)
    line = re.sub(r"(Triggers\.becomesBlocked\(\)) PER_BLOCKER\b", r"\1 + TriggerOptions.EACH_BLOCKER", line)
    line = line.replace("addEffect(slot, effect, TriggerOptions.EACH_BLOCKER)",
                        "addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, effect)")
    return attach(line) if line != original else line


def main():
    total = 0
    for path in FILES:
        if not os.path.exists(path):
            continue
        src = open(path).read()
        new = src
        for old, rep in EXACT.get(path, []):
            if new.count(old) != 1:
                raise SystemExit("%s: %r applies %d times" % (path, old[:60], new.count(old)))
            new = new.replace(old, rep)
        new = "\n".join(rewrite_line(l) for l in new.split("\n"))
        if new != src:
            changed = sum(1 for a, b in zip(src.split("\n"), new.split("\n")) if a != b)
            total += changed
            open(path, "w").write(new)
            print("%4d lines  %s" % (changed, path))
    print("%d lines rewritten" % total, file=sys.stderr)


if __name__ == "__main__":
    main()
