#!/usr/bin/env python3
"""Step 15, run 17z14: the planes that wrote one Oracle chaos ability as several `addTrigger` calls.

The planar reader (`PlanechaseService.trigger`) reads each triggered ability whole, as one stack object (CR 603.3), so
a chaos ability split into several `addTrigger` calls would be several stack objects (its old per-effect loop made
them several objects too, but for its multi-target batch, which joined Raven's Run's three and Valor's Reach's two).
Each plane below was read against its Oracle text and has one chaos ability; its calls become one `addTrigger` with
every effect, in Oracle order. Raven's Run's three target groups ("target creature", "another target creature", "a
third target creature") bind each effect with `bindEffect`; Valor's Reach's "up to two target creatures" binds its
untap. Windriddle Palaces' "each player mills a card" is one `MillEffect(1, MillRecipient.EACH_PLAYER)`.

Usage: step15-run17z14-cards.py [LETTER...] (run per letter module: b, k, r, v, w; no letter means all). Fails if an
edit didn't apply; idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"

# card -> [(old, new)]
EDITS = {
    "BadWolfBay": [(
        """        addTrigger(Triggers.chaosEnsues(), new PlaneswalkEffect());
        addTrigger(Triggers.chaosEnsues(), new ShuffleCreaturesEnteringFromExileEffect());
""",
        """        // "When chaos ensues, cards can't enter from exile this turn. Then planeswalk." is one ability.
        addTrigger(Triggers.chaosEnsues(), new ShuffleCreaturesEnteringFromExileEffect(), new PlaneswalkEffect());
""")],
    "Kessig": [(
        """        addTrigger(Triggers.chaosEnsues(), new BoostAllOwnCreaturesEffect(2, 2));
        addTrigger(Triggers.chaosEnsues(),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
        addTrigger(Triggers.chaosEnsues(),
                new GrantSubtypeUntilEndOfTurnEffect(CardSubtype.WEREWOLF, GrantScope.OWN_CREATURES));
""",
        """        addTrigger(Triggers.chaosEnsues(),
                new BoostAllOwnCreaturesEffect(2, 2),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES),
                new GrantSubtypeUntilEndOfTurnEffect(CardSubtype.WEREWOLF, GrantScope.OWN_CREATURES));
""")],
    "RavensRun": [(
        """        target(TargetFilters.creature()).addTrigger(Triggers.chaosEnsues(),
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));
        target(TargetFilters.creature()).addTrigger(Triggers.chaosEnsues(),
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 2));
        target(TargetFilters.creature()).addTrigger(Triggers.chaosEnsues(),
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 3));
""",
        """        // One ability of three target groups: "target creature", "another target creature", "a third target
        // creature".
        CardEffect oneCounter = new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1);
        CardEffect twoCounters = new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 2);
        CardEffect threeCounters = new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 3);
        addTrigger(Triggers.chaosEnsues(), oneCounter, twoCounters, threeCounters);
        target(TargetFilters.creature()).bindEffect(oneCounter);
        target(TargetFilters.creature()).bindEffect(twoCounters);
        target(TargetFilters.creature()).bindEffect(threeCounters);
"""), (
        "import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;\n",
        "import com.github.laxika.magicalvibes.model.effect.CardEffect;\n"
        "import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;\n")],
    "ValorsReach": [(
        """        target(TargetFilters.creatureYouControl(), 0, 2)
                .addTrigger(Triggers.chaosEnsues(),
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS));
        addTrigger(Triggers.chaosEnsues(),
                new ConditionalEffect(
                        new ControllerMainPhase(),
                        new AdditionalCombatMainPhaseEffect(1)));
""",
        """        // One ability: "untap up to two target creatures your team controls. If it's a main phase, there is an
        // additional combat phase after this phase, followed by an additional main phase."
        CardEffect untap = new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS);
        addTrigger(Triggers.chaosEnsues(),
                untap,
                new ConditionalEffect(
                        new ControllerMainPhase(),
                        new AdditionalCombatMainPhaseEffect(1)));
        target(TargetFilters.creatureYouControl(), 0, 2).bindEffect(untap);
"""), (
        "import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;\n",
        "import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;\n"
        "import com.github.laxika.magicalvibes.model.effect.CardEffect;\n")],
    "WindriddlePalaces": [(
        """        addTrigger(Triggers.chaosEnsues(), new MillEffect(1, MillRecipient.CONTROLLER));
        addTrigger(Triggers.chaosEnsues(), new MillEffect(1, MillRecipient.EACH_OPPONENT));
""",
        """        addTrigger(Triggers.chaosEnsues(), new MillEffect(1, MillRecipient.EACH_PLAYER));
""")],
}


def main(letters):
    failed = []
    for name, edits in EDITS.items():
        letter = name[0].lower()
        if letters and letter not in letters:
            continue
        path = ROOT / letter / PKG / letter / f"{name}.java"
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                failed.append(f"{name}: {old.strip().splitlines()[0]}")
                continue
            text = text.replace(old, new)
        path.write_text(text)
        if text.count("addTrigger(Triggers.chaosEnsues()") != 1:
            failed.append(f"{name}: its chaos ability is not one addTrigger")
    if failed:
        sys.exit("Edits not applied:\n" + "\n".join(failed))


if __name__ == "__main__":
    main(set(sys.argv[1:]))
