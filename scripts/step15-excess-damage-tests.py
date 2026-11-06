#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 11: Overclocked Electromancer's intervening "if" is judged as the ability triggers.

Idempotent; run once per letter test module:

    for m in magical-vibes-card-test/*/; do python3 scripts/step15-excess-damage-tests.py "$m"; done

Adds two tests to OverclockedElectromancerTest: without excess damage the ability doesn't trigger at all (CR 603.4),
so the log shows no trigger, not just no energy; with excess damage it shows exactly one. (The harness resolves
the trigger as combat damage ends, so the stack can't be inspected.)
"""
import pathlib
import sys

ANCHOR = """    @Test
    void cannotPayWithFewerThanThreeEnergy() {"""

IMPORT = "import com.github.laxika.magicalvibes.model.GameLogEntry;\n"
IMPORT_AFTER = "import com.github.laxika.magicalvibes.model.CounterType;\n"

TESTS = """    @Test
    void doesNotTriggerWithoutExcessDamage() {
        Permanent electromancer = addCreatureReady(player1, new OverclockedElectromancer());
        addCreatureReady(player2, new HillGiant());

        electromancer.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        String triggers = electromancer.getCard().getName() + "'s ability triggers";
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains(triggers));
    }

    @Test
    void triggersOnceWithExcessDamage() {
        Permanent electromancer = addCreatureReady(player1, new OverclockedElectromancer());
        Permanent hillGiant = addCreatureReady(player2, new HillGiant());
        hillGiant.addMarkedDamage(null, 2);

        electromancer.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        String triggers = electromancer.getCard().getName() + "'s ability triggers";
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).filteredOn(log -> log.contains(triggers))
                .hasSize(1);
    }

"""


def main(module):
    for path in sorted(pathlib.Path(module).rglob("OverclockedElectromancerTest.java")):
        if "/build/" in str(path):
            continue
        src = path.read_text()
        if "void doesNotTriggerWithoutExcessDamage()" in src:
            continue
        if src.count(ANCHOR) != 1:
            raise SystemExit(f"{path}: anchor not found once")
        src = src.replace(ANCHOR, TESTS + ANCHOR)
        if IMPORT not in src:
            src = src.replace(IMPORT_AFTER, IMPORT_AFTER + IMPORT)
        path.write_text(src)
        print(f"converted {path}")


if __name__ == "__main__":
    main(sys.argv[1])
