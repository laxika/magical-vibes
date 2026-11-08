#!/usr/bin/env python3
"""Step 15, run 17x5b: the card tests of the run.

- KurkeshOnakkeAncientTest: a new case, the rules fix: "an ability of an artifact" is an ability of an artifact
  permanent (CR 109.2), so cycling an artifact card from the hand (Ketria Crystal) doesn't trigger Kurkesh. The old
  record judged the cycling ability's stack entry by its card's type and offered the {R} copy.

Each edit is (old, new): the old text is replaced; a file that already has the new text is left alone. A new test is
added before the class's closing brace, with its imports. Idempotent. Per letter module.

Usage: step15-run17x5b-tests.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

KURKESH_TEST = '''
    @Test
    @DisplayName("Cycling an artifact card from the hand does not trigger Kurkesh")
    void cyclingAnArtifactCardDoesNotTrigger() {
        harness.addToBattlefield(player1, new KurkeshOnakkeAncient());
        harness.setHand(player1, List.of(new KetriaCrystal()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(KetriaCrystal.class);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
'''

TESTS = {
    "KurkeshOnakkeAncientTest": (
        [("@CardUsed({KurkeshOnakkeAncient.class, RodOfRuin.class, LlanowarElves.class,\n"
          "        ProdigalPyromancer.class, Meteorite.class})",
          "@CardUsed({KurkeshOnakkeAncient.class, RodOfRuin.class, LlanowarElves.class,\n"
          "        ProdigalPyromancer.class, Meteorite.class, KetriaCrystal.class})")],
        [("void cyclingAnArtifactCardDoesNotTrigger()", KURKESH_TEST)],
        ["java:java.util.List"]),
}


def import_line(fqcn):
    return f"import {fqcn[5:]};" if fqcn.startswith("java:") else f"import {PKG}.{fqcn};"


def add_import(text, fqcn):
    line = import_line(fqcn)
    if line in text:
        return text
    imports = list(re.finditer(r"^import (?!static )[^;]+;$", text, re.M))
    pos = imports[-1].end()
    return text[:pos] + "\n" + line + text[pos:]


def convert_file(path, name):
    if name not in TESTS:
        return
    text = path.read_text()
    original = text
    edits, added, imports = TESTS[name]
    for old, new in edits:
        if old in text:
            text = text.replace(old, new)
        elif new not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
    for marker, test in added:
        if marker not in text:
            text = text.rstrip()
            if not text.endswith("}"):
                raise SystemExit(f"{name}: no closing brace")
            text = text[:-1].rstrip("\n") + "\n" + test + "}\n"
    for fqcn in imports:
        text = add_import(text, fqcn)
    if text != original:
        path.write_text(text)
        print(f"converted {name}")


def main():
    modules = sorted(set("abcdefghijklmnopqrstuvwxyz")) if sys.argv[1:] == ["all"] else sys.argv[1:]
    if not modules:
        raise SystemExit(__doc__)
    for module in modules:
        root = ROOT / f"magical-vibes-card-test/{module}/src/test/java"
        if not root.exists():
            continue
        for path in sorted(root.rglob("*.java")):
            convert_file(path, path.stem)


if __name__ == "__main__":
    main()
