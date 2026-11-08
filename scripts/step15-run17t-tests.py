#!/usr/bin/env python3
"""Step 15, run 17t: the card tests of the converted cards.

- Banish into Fable (ELD): "copy it if you control an artifact, then copy it if you control an enchantment" is one
  ability (CR 603.3), so both copies are on the stack before either resolves (a rules fix; the two abilities it was
  resolved one copy before the second ability). The two-copies case now asks both new-target questions as the one
  ability resolves and checks the stack holds one copy trigger, then two copies.
- Hedron Blade (BFZ): "becomes blocked by one or more colorless creatures", now the trigger's filter, triggers once
  however many of the blockers are colorless, and when a colored creature blocks too (CR 509.3c).

Idempotent.

Usage: step15-run17t-tests.py <letter test module dir>...
"""
import pathlib
import sys

BANISH_OLD = """        harness.castInstant(player1, 0, originalTarget.getId());
        resolveCopyTo(firstCopyTarget);
        resolveCopyTo(secondCopyTarget);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Knight")).hasSize(3);
"""
BANISH_NEW = """        harness.castInstant(player1, 0, originalTarget.getId());
        // "Copy it if …, then copy it if …" is one ability (CR 603.3): both copies are made as it resolves.
        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);
        harness.passBothPriorities();
        chooseNewTargetForCopy(firstCopyTarget);
        chooseNewTargetForCopy(secondCopyTarget);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Knight")).hasSize(3);
"""
BANISH_HELPER_OLD = """    private void resolveCopyTo(Permanent target) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
"""
BANISH_HELPER_NEW = """    private void chooseNewTargetForCopy(Permanent target) {
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
    }
"""

HEDRON_ANCHOR = """    private void declareBlockers() {
"""
HEDRON_NEW = """    @Test
    @DisplayName("Blocked by a colored and two colorless creatures, the ability triggers once")
    void triggersOnceForSeveralColorlessBlockers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new Ornithopter());
        addCreatureReady(player2, new Ornithopter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(gd.stack).filteredOn(entry -> entry.getCard().getName().equals("Hedron Blade")).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
    }

""" + HEDRON_ANCHOR

EDITS = {
    "HedronBladeTest": [
        (HEDRON_ANCHOR, HEDRON_NEW),
    ],
    "BanishIntoFableTest": [
        ("import com.github.laxika.magicalvibes.model.Permanent;\n",
         "import com.github.laxika.magicalvibes.model.Permanent;\n"
         "import com.github.laxika.magicalvibes.model.StackEntry;\n"
         "import com.github.laxika.magicalvibes.model.StackEntryType;\n"),
        (BANISH_OLD, BANISH_NEW),
        (BANISH_HELPER_OLD, BANISH_HELPER_NEW),
    ],
}

DONE_MARKERS = {
    "HedronBladeTest": "triggersOnceForSeveralColorlessBlockers",
    "BanishIntoFableTest": "chooseNewTargetForCopy",
}


def main():
    changed = []
    seen = set()
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/test/java/**/*.java")):
            edits = EDITS.get(path.stem)
            if edits is None:
                continue
            seen.add(path.stem)
            source = path.read_text()
            if DONE_MARKERS[path.stem] in source:
                continue
            result = source
            for old, new in edits:
                if result.count(old) != 1:
                    raise SystemExit(f"{path}: expected one occurrence of {old!r}")
                result = result.replace(old, new)
            path.write_text(result)
            changed.append(str(path))
    for path in changed:
        print("converted", path)
    print(f"{len(changed)} converted, {len(seen)} seen")


if __name__ == "__main__":
    main()
