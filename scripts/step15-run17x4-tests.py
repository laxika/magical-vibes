#!/usr/bin/env python3
"""Step 15, run 17x4: the card tests that named a removed form.

- ObNixilisReignitedTest: the -8 emblem test no longer inspects the emblem's static effects for the removed
  `EmblemControllerLosesLifeOnAnyPlayerDrawEffect` marker (the emblem's ability is a `TriggeredAbility` now); its
  draws through the engine already assert what the emblem does.

Idempotent: a file already converted is left alone. Per letter module.

Usage: step15-run17x4-tests.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent

EDITS = {
    ("o", "ObNixilisReignitedTest"): [
        ("""        assertThat(emblem.controllerId()).isEqualTo(player2.getId());
        assertThat(emblem.staticEffects()).singleElement()
                .isEqualTo(new EmblemControllerLosesLifeOnAnyPlayerDrawEffect(2));
""", """        assertThat(emblem.controllerId()).isEqualTo(player2.getId());
"""),
        ("import com.github.laxika.magicalvibes.model.effect.EmblemControllerLosesLifeOnAnyPlayerDrawEffect;\n", ""),
    ],
}


def main():
    modules = sorted(set("abcdefghijklmnopqrstuvwxyz")) if sys.argv[1:] == ["all"] else sys.argv[1:]
    if not modules:
        raise SystemExit(__doc__)
    for (module, name), edits in EDITS.items():
        if module not in modules:
            continue
        path = (ROOT / f"magical-vibes-card-test/{module}/src/test/java/com/github/laxika/magicalvibes/cards"
                / module / f"{name}.java")
        text = path.read_text()
        original = text
        for old, new in edits:
            if old in text:
                text = text.replace(old, new)
            elif new and new not in text:
                raise SystemExit(f"{name}: expected text not found:\n{old}")
        text = re.sub(r"\n\n\n+", "\n\n", text)
        if text != original:
            path.write_text(text)
            print(f"converted {name}")


if __name__ == "__main__":
    main()
