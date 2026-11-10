#!/usr/bin/env python3
"""Step 15, run 17z13e7: Sigurd, Jarl of Ravensthorpe says the target its class-keyed lore-counter handler chose.

`SagaCounterTriggerCollectorService.handleLoreCounterPlaced` reached "whenever you put a lore counter on a Saga you
control, put a +1/+1 counter on up to one other target creature" by its effect's class and built the target choice
itself: "another creature" (the source excluded by its permanent id) and "up to one" were the handler's, not the
card's. Through the watch's default the ability chooses its target from the card's declared target group, so the card
declares it: `target(another creature, 0, 1)` bound to the lore-counter ability (CR 603.3d, chosen as it goes on the
stack); the effect keeps its creature restriction.

Run per letter module: step15-run17z13e7-cards.py [LETTER...] (s). Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
PKG = "com/github/laxika/magicalvibes/cards"

EDITS = {
    "s/SigurdJarlOfRavensthorpe.java": [
        ("import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;\n",
         "import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;\n"),
        ("import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;\n",
         "import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;\n"),
        ("""        addTrigger(Triggers.youPutLoreCounterOnSagaYouControl(),""",
         """        // "Up to one other target creature", chosen as the ability goes on the stack.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))),
                "Target must be another creature"), 0, 1)
                .addTrigger(Triggers.youPutLoreCounterOnSagaYouControl(),"""),
    ],
}


def main():
    letters = set(sys.argv[1:]) or {key.split("/")[0] for key in EDITS}
    for key, edits in EDITS.items():
        if key.split("/")[0] not in letters:
            continue
        path = CARDS / key.split("/")[0] / "src/main/java" / PKG / key
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if old not in text:
                sys.exit(f"{key}: pattern not found: {old[:60]!r}")
            text = text.replace(old, new, 1)
        path.write_text(text)


if __name__ == "__main__":
    main()
