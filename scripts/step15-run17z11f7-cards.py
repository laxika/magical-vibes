#!/usr/bin/env python3
"""Step 15, run 17z11f7: Bogardan Hellkite's "any number of targets" is an any-target group.

Its enter ability's targets are a declared target group, which without a filter admits only creatures as permanents;
"any target" also admits planeswalkers and battles (CR 115.4). The staged division that card tests used to supply
skipped the target choice and hid it.

Idempotent; run per letter module. Usage: step15-run17z11f7-cards.py [repository-root]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
CARD = ROOT / "magical-vibes-card/b/src/main/java/com/github/laxika/magicalvibes/cards/b/BogardanHellkite.java"

EDITS = [
    ("""import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
""", """import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
"""),
    ("""import com.github.laxika.magicalvibes.model.trigger.Triggers;
""", """import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
"""),
    ("""        target(0, 5).addTrigger(Triggers.enters(ObjectRelation.THIS),""",
     """        target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target"), 0, 5).addTrigger(Triggers.enters(ObjectRelation.THIS),"""),
]


def main() -> None:
    text = CARD.read_text(encoding="utf-8")
    original = text
    for old, new in EDITS:
        applied = new in text if old in new else old not in text
        if applied:
            continue
        if text.count(old) != 1:
            raise SystemExit(f"{CARD.name}: expected one occurrence of {old[:60]!r}")
        text = text.replace(old, new)
    if text != original:
        CARD.write_text(text, encoding="utf-8")
        print("converted", CARD.name)
    else:
        print("already converted", CARD.name)


if __name__ == "__main__":
    main()
