#!/usr/bin/env python3
"""Step 15, run 17x5: the card classes whose leaves-the-battlefield or death ability a collector rebuilt.

- "When this leaves the battlefield, you lose the game" (Lich's Mastery, Lich's Duel Mastery, Nine Lives, Nefarious
  Lich): the existing `ControllerLosesGameEffect()`; `ControllerLosesGameOnLeavesEffect` is gone.
- Vodalian War Machine's "destroy all Merfolk tapped this turn to pay for its abilities": the existing
  `DestroyAllPermanentsEffect` with the new `PermanentTappedForSourceAbilitiesThisTurnPredicate`;
  `DestroyMerfolkTappedForSourceAbilitiesEffect` is gone.
- `ExileGraveyardCardsEffect` lost its `affectedPlayerId` component (no collector writes it since run 17x4): the
  fourth argument of every `new ExileGraveyardCardsEffect(...)` with four or more arguments is removed (it must be
  `null`).

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module. `--other` runs the `ExileGraveyardCardsEffect` rewrite over the engine, AI, application and test-fixture
sources instead (the same mechanical edit, kept in one place).

Usage: step15-run17x5-cards.py <letter> [<letter> ...]   (or "all")
       step15-run17x5-cards.py --other
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

LOSE = ("new ControllerLosesGameOnLeavesEffect()", "new ControllerLosesGameEffect()")
LOSE_IMPORTS = (["model.effect.ControllerLosesGameEffect"], ["model.effect.ControllerLosesGameOnLeavesEffect"])

CARDS = {
    "LichsMastery": ([LOSE], *LOSE_IMPORTS),
    "LichsDuelMastery": ([LOSE], *LOSE_IMPORTS),
    "NineLives": ([LOSE], *LOSE_IMPORTS),
    "NefariousLich": ([LOSE], *LOSE_IMPORTS),
    "VodalianWarMachine": ([("addTrigger(Triggers.dies(ObjectRelation.THIS), new DestroyMerfolkTappedForSourceAbilitiesEffect());",
                             "addTrigger(Triggers.dies(ObjectRelation.THIS), new DestroyAllPermanentsEffect(\n"
                             "                new PermanentAllOfPredicate(List.of(merfolk,\n"
                             "                        new PermanentTappedForSourceAbilitiesThisTurnPredicate()))));")],
                           ["model.effect.DestroyAllPermanentsEffect", "model.filter.PermanentAllOfPredicate",
                            "model.filter.PermanentTappedForSourceAbilitiesThisTurnPredicate"],
                           ["model.effect.DestroyMerfolkTappedForSourceAbilitiesEffect"]),
}

CALL = "new ExileGraveyardCardsEffect("


def split_args(text, start):
    """The top-level argument spans of the call whose '(' is at text[start - 1]; returns (spans, close index)."""
    depth = 0
    spans = []
    arg_start = start
    i = start
    in_str = None
    while i < len(text):
        ch = text[i]
        if in_str:
            if ch == "\\":
                i += 2
                continue
            if ch == in_str:
                in_str = None
        elif ch in "\"'":
            in_str = ch
        elif ch in "([{":
            depth += 1
        elif ch in ")]}":
            if depth == 0:
                spans.append((arg_start, i))
                return spans, i
            depth -= 1
        elif ch == "," and depth == 0:
            spans.append((arg_start, i))
            arg_start = i + 1
        i += 1
    raise SystemExit("unbalanced call")


def drop_affected_player(text, name):
    out = []
    pos = 0
    while True:
        idx = text.find(CALL, pos)
        if idx < 0:
            out.append(text[pos:])
            return "".join(out)
        open_paren = idx + len(CALL)
        spans, close = split_args(text, open_paren)
        if len(spans) < 4:
            out.append(text[pos:close])
            pos = close
            continue
        fourth = text[spans[3][0]:spans[3][1]].strip()
        if fourth in ("true", "false"):
            # Already converted: the fourth argument is exactTargetCount now.
            out.append(text[pos:close])
            pos = close
            continue
        if fourth != "null":
            raise SystemExit(f"{name}: affectedPlayerId argument is not null: {fourth}")
        # Remove ", <4th>" : from the end of the 3rd argument to the end of the 4th.
        out.append(text[pos:spans[2][1]])
        pos = spans[3][1]


def add_import(text, fqcn):
    line = f"import {PKG}.{fqcn};"
    if line in text:
        return text
    imports = list(re.finditer(r"^import [^;]+;$", text, re.M))
    pos = imports[0].start()
    return text[:pos] + line + "\n" + text[pos:]


def remove_unused_import(text, fqcn):
    simple = fqcn.rsplit(".", 1)[1]
    line = f"import {PKG}.{fqcn};\n"
    if line in text and not re.search(r"\b" + simple + r"\b", text.replace(line, "")):
        text = text.replace(line, "")
    return text


def convert_file(path, name):
    text = path.read_text()
    original = text
    if name in CARDS:
        edits, imports, removed = CARDS[name]
        for old, new in edits:
            if old in text:
                text = text.replace(old, new)
            elif new not in text:
                raise SystemExit(f"{name}: expected text not found:\n{old}")
        for fqcn in imports:
            text = add_import(text, fqcn)
        for fqcn in removed:
            text = remove_unused_import(text, fqcn)
    if CALL in text:
        text = drop_affected_player(text, name)
    if text != original:
        path.write_text(text)
        print(f"converted {name}")


def main():
    if sys.argv[1:] == ["--other"]:
        roots = [ROOT / m for m in ("magical-vibes-engine/src", "magical-vibes-ai/src", "magical-vibes-domain/src",
                                    "magical-vibes-application/src")]
        for root in roots:
            for path in sorted(root.rglob("*.java")):
                if path.name == "ExileGraveyardCardsEffect.java":
                    continue
                convert_file(path, path.stem)
        return
    modules = sorted(set("abcdefghijklmnopqrstuvwxyz")) + ["shared"] if sys.argv[1:] == ["all"] else sys.argv[1:]
    if not modules:
        raise SystemExit(__doc__)
    for module in modules:
        for base in (f"magical-vibes-card/{module}/src/main/java", f"magical-vibes-card-test/{module}/src/test/java"):
            root = ROOT / base
            if not root.exists():
                continue
            for path in sorted(root.rglob("*.java")):
                convert_file(path, path.stem)


if __name__ == "__main__":
    main()
