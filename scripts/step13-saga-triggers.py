#!/usr/bin/env python3
"""Moves card classes and tests off the Saga EffectSlots (effect-slot-refactor.md step 13, family 10).

Every chapter registration becomes a triggered ability with a ChapterTrigger, and the two final-chapter slots a
FinalChapterTrigger. Card.addTrigger adds every effect of one chapter to the same ability, as the chapter slots
grouped them (LegacySlotGrouping.ALL), so each registration is converted on its own. SLOTS, read from
scripts/step13-saga-slots.tsv, maps each slot to its trigger:

* addEffect(EffectSlot.X, e) -> addTrigger(T, e), on the card or chained on a target group;
  addSimultaneousEffects(EffectSlot.X, ...) -> addSimultaneousTrigger(T, ...);
* setSagaChapterTargetFilter / setSagaChapterTargetRequired / setSagaChapterTargetGroups and their getters take the
  chapter number: setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, f) -> setSagaChapterTargetFilter(2, f);
* in card classes, a helper parameter or loop variable of type EffectSlot holding a chapter becomes an int chapter number:
  addChapter(EffectSlot chapter) { addEffect(chapter, e); } -> addChapter(int chapter) {
  addTrigger(Triggers.chapter(chapter), e); }, new EffectSlot[]{...} -> new int[]{...}, and the remaining chapter
  slot literals (the helper's arguments, the loop's list) become their numbers;
* in tests, getEffects / getEffectRegistrations of a slot read the trigger.

Usage: python3 scripts/step13-saga-triggers.py DIR [DIR ...]
       python3 scripts/step13-saga-triggers.py --polish FILE [FILE ...]  (the polish pass alone, on converted files)
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
step11 = __import__("step11-death-triggers")

ROWS = [line.rstrip("\n").split("\t") for line in open(os.path.join(os.path.dirname(__file__), "step13-saga-slots.tsv"))
        if not line.startswith("#")]
SLOTS = {row[0]: "Triggers.%s" % row[1] for row in ROWS}
CHAPTERS = {row[0]: int(re.match(r"chapter\((\d)\)", row[1]).group(1)) for row in ROWS
            if row[1].startswith("chapter(")}

SLOT = r"(?:com\.github\.laxika\.magicalvibes\.model\.)?EffectSlot\.(%s)\b" % "|".join(SLOTS)
CHAPTER_SLOT = r"(?:com\.github\.laxika\.magicalvibes\.model\.)?EffectSlot\.(%s)\b" % "|".join(CHAPTERS)
TARGET_METHODS = r"(setSagaChapterTargetFilter|setSagaChapterTargetRequired|setSagaChapterTargetGroups|" \
                 r"getSagaChapterTargetFilters|isSagaChapterTargetRequired|getSagaChapterTargetGroups)"

MANUAL = {
}


def convert_variables(src, card):
    """An EffectSlot parameter or loop variable that holds a chapter becomes the chapter number."""
    names = re.findall(r"\bEffectSlot\s+(\w+)\s*[,):]", src)
    for name in names:
        src = re.sub(r"\bEffectSlot(\s+%s\s*[,):])" % name, r"int\1", src)
        for m in reversed(list(re.finditer(r"(addEffect|addSimultaneousEffects)\(\s*%s\s*,(\s*)" % name, src))):
            close = step11.find_close(src, src.index("(", m.start()))
            if "TriggerMode." in src[m.end():close]:
                raise ValueError("%s: a TriggerMode on chapter variable %s" % (card, name))
            head = "addTrigger" if m.group(1) == "addEffect" else "addSimultaneousTrigger"
            sep = "," + m.group(2) if "\n" in m.group(2) else ", "
            src = src[:m.start()] + "%s(Triggers.chapter(%s)%s" % (head, name, sep) + src[m.end():]
        if re.search(r"\b(addEffect|addSimultaneousEffects|getEffects|getEffectRegistrations)\(\s*%s\b" % name, src):
            raise ValueError("%s: chapter variable %s is read as a slot" % (card, name))
    src = src.replace("new EffectSlot[]", "new int[]")
    return src


def polish(src):
    """A chapter helper's parameter is named chapter, and a loop's chapter numbers fit on one line."""
    if re.search(r"\(int slot\)", src) and not re.search(r"\bint chapter\b", src):
        src = re.sub(r"\bslot\b", "chapter", src)
    src = re.sub(r"(for \(int chapter : (?:Set\.of\(|List\.of\(|new int\[\]\{))\s*((?:\d,\s*)*\d)(\s*)([)}]\))",
                 lambda m: m.group(1) + re.sub(r",\s*", ", ", m.group(2)) + m.group(4), src)
    return src


def convert_target_methods(src):
    return re.sub(r"\b%s\(\s*%s" % (TARGET_METHODS, CHAPTER_SLOT),
                  lambda m: "%s(%d" % (m.group(1), CHAPTERS[m.group(2)]), src)


def convert_calls(src, card):
    call = re.compile(r"(addEffect|addSimultaneousEffects)\((\s*)%s\s*,(\s*)" % SLOT)
    for m in reversed(list(call.finditer(src))):
        close = step11.find_close(src, src.index("(", m.start()))
        head, slot, gap = m.group(1), m.group(3), m.group(4)
        trigger = SLOTS[slot]
        sep = "," + gap if "\n" in gap else ", "
        rest = src[m.end():close]
        if "TriggerMode." in rest:
            raise ValueError("%s: a %s registration with a TriggerMode" % (card, slot))
        name = "addTrigger" if head == "addEffect" else "addSimultaneousTrigger"
        src = src[:m.start()] + "%s(%s%s%s)" % (name, trigger, sep, rest) + src[close + 1:]
    return src


def convert_readers(src):
    src = re.sub(r"\.getEffects\(\s*%s\s*\)" % SLOT, lambda m: ".getTriggerEffects(%s)" % SLOTS[m.group(1)], src)
    src = re.sub(r"\.getEffectRegistrations\(\s*%s\s*\)" % SLOT,
                 lambda m: ".getTriggerEffectRegistrations(%s)" % SLOTS[m.group(1)], src)
    return src


def convert_literals(src, card):
    """A chapter slot left over is a helper's argument or an element of a loop's list: its number."""
    src = re.sub(CHAPTER_SLOT, lambda m: str(CHAPTERS[m.group(1)]), src)
    if re.search(SLOT, src):
        raise ValueError("%s: %s left" % (card, re.search(SLOT, src).group(1)))
    return src


def convert_comments(src):
    names = r"\b(%s)\b" % "|".join(SLOTS)
    return re.sub(r"//.*", lambda m: re.sub(names, lambda n: SLOTS[n.group(1)], m.group(0)), src)


def wrap_long_lines(src):
    lines = []
    for line in src.split("\n"):
        m = re.match(r"^(\s*)(.*?(?:addTrigger|addSimultaneousTrigger)\(Triggers\.\w+\(\w*\),) (.+)$", line)
        if len(line) > 120 and m:
            lines.append(m.group(1) + m.group(2))
            lines.append(m.group(1) + "        " + m.group(3))
        else:
            lines.append(line)
    return "\n".join(lines)


def drop_unused_imports(src):
    body = re.sub(r"^import .*$", "", src, flags=re.M)
    for fqcn in ("com.github.laxika.magicalvibes.model.EffectSlot",):
        simple = fqcn.rsplit(".", 1)[1]
        if not re.search(r"\b%s\b" % simple, body):
            src = re.sub(r"^import %s;\n" % re.escape(fqcn), "", src, flags=re.M)
    return src


def main():
    if sys.argv[1] == "--polish":
        for path in sys.argv[2:]:
            src = open(path).read()
            out = polish(src)
            if out != src:
                open(path, "w").write(out)
                print("polished:", path)
        return
    slot_re = re.compile(SLOT)
    for root_dir in sys.argv[1:]:
        for root, _, files in os.walk(root_dir):
            for name in sorted(files):
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                if not slot_re.search(src):
                    continue
                card = name[:-5]
                for old, new in MANUAL.get(card, []):
                    if old not in src:
                        raise ValueError("%s: manual text not found" % card)
                    src = src.replace(old, new)
                if "/magical-vibes-card/" in path:
                    src = convert_variables(src, card)
                src = convert_target_methods(src)
                src = convert_calls(src, card)
                src = convert_readers(src)
                src = convert_comments(src)
                src = convert_literals(src, card)
                src = polish(src)
                src = wrap_long_lines(src)
                src = step11.fix_imports(src)
                src = drop_unused_imports(src)
                open(path, "w").write(src)
                print("converted:", path)


if __name__ == "__main__":
    main()
