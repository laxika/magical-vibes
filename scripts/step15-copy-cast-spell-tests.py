#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 17b: the card tests of the copy-cast-spell run.

Idempotent; run once per letter module:

    for m in magical-vibes-card-test/*/; do python3 scripts/step15-copy-cast-spell-tests.py "$m"; done

- Pyromancer Ascension: "whenever you cast … while this enchantment has two or more quest counters" is an event
  condition, checked only as the spell is cast. The card's rulings say a player can't remove the Ascension in
  response to keep the ability from triggering, so the case that removed the counters in response and expected no
  copy now expects the copy (it is a rules fix).
- Alania, Divergent Storm: the copy is made even if the spell left the stack before the ability resolved: a targeted
  cast trigger's entry takes the triggering spell from the target choice (CR 608.2h).
"""
import pathlib
import sys

PYROMANCER_OLD = '''    @Test
    @DisplayName("Removing the counters after casting makes the intervening-if fail on resolution (CR 603.4)")
    void removingCountersAfterCastingPreventsCopy() {
        var ascension = addAscension(2);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        ascension.setCounterCount(CounterType.QUEST, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stagedSpellCopies).isEmpty();
        assertThat(gd.stack.stream().filter(entry -> entry.isCopy())).isEmpty();
    }
'''
PYROMANCER_NEW = '''    @Test
    @DisplayName("Removing the counters after casting doesn't stop the copy: \\"while\\" is checked only as the spell is cast")
    void removingCountersAfterCastingStillCopies() {
        var ascension = addAscension(2);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        ascension.setCounterCount(CounterType.QUEST, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stagedSpellCopies).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack.stream().filter(entry -> entry.getCard().getName().equals("Burst Lightning")).count())
                .isGreaterThanOrEqualTo(2);
    }
'''

ALANIA_ANCHOR = "    private void acceptTriggerAndCreateCopy() {\n"
ALANIA_NEW = '''    @Test
    @DisplayName("The copy is made even if the spell left the stack before the ability resolved")
    void copiesSpellThatLeftTheStack() {
        harness.addToBattlefield(player1, new AlaniaDivergentStorm());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        gd.stack.removeIf(entry -> !entry.isCopy() && entry.getCard().getName().equals("Shock"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(8);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).singleElement()
                .satisfies(copy -> assertThat(copy.getCard().getName()).isEqualTo("Shock"));
    }

'''

TESTS = {
    "PyromancerAscensionTest.java": [(PYROMANCER_OLD, PYROMANCER_NEW)],
    "AlaniaDivergentStormTest.java": [(ALANIA_ANCHOR, ALANIA_NEW + ALANIA_ANCHOR)],
}


def convert(path, replacements):
    src = path.read_text()
    converted = src
    for old, new in replacements:
        if new in converted:
            continue
        if old not in converted:
            raise SystemExit(f"{path}: neither the old nor the new form of {old[:60]!r}")
        converted = converted.replace(old, new, 1)
    if converted != src:
        path.write_text(converted)
        print(f"converted {path}")


def main():
    module = pathlib.Path(sys.argv[1])
    for path in sorted(module.glob("src/test/java/**/*.java")):
        if path.name in TESTS:
            convert(path, TESTS[path.name])


if __name__ == "__main__":
    main()
