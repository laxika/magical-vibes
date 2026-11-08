#!/usr/bin/env python3
"""Step 15, run 17u: card tests for the run's changes.

- Cult of Skaro (WHO): "choose one at random" is chosen as the ability goes on the stack (CR 603.3c, 700.2b), before
  anyone can respond: the entry on the stack holds one mode, not the random choice.

Idempotent.

Usage: step15-run17u-tests.py <letter test module dir>...
"""
import pathlib
import sys

EDITS = {
    "CultOfSkaroTest": [
        ("import com.github.laxika.magicalvibes.model.TurnStep;\n",
         "import com.github.laxika.magicalvibes.model.TurnStep;\n"
         "import com.github.laxika.magicalvibes.model.effect.RandomChoiceEffect;\n"),
        ("""    @Test
    void doesNotTriggerWhenOnlyAnotherCreatureAttacks() {""",
         """    @Test
    void randomModeIsChosenAsTheAbilityGoesOnTheStack() {
        addCreatureReady(player1, new CultOfSkaro());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEffectsToResolve())
                    .hasSize(1)
                    .noneMatch(RandomChoiceEffect.class::isInstance);
        });
    }

    @Test
    void doesNotTriggerWhenOnlyAnotherCreatureAttacks() {"""),
    ],
}


def main(dirs):
    for d in dirs:
        root = pathlib.Path(d)
        for name, edits in EDITS.items():
            for path in root.rglob(f"cards/*/{name}.java"):
                text = path.read_text()
                original = text
                for old, new in edits:
                    if new in text:
                        continue
                    if old not in text:
                        raise SystemExit(f"{path}: expected text not found: {old!r}")
                    text = text.replace(old, new)
                if text != original:
                    path.write_text(text)
                    print(f"changed {path}")


if __name__ == "__main__":
    main(sys.argv[1:])
