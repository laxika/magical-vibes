#!/usr/bin/env python3
"""Step 15 follow-up: copied triggered abilities keep the trigger event.

Adds card tests where a copy of a triggered ability reads the permanent its trigger event was about:
- StrionicResonatorTest: a copy of Hangarback Walker's death trigger counts the +1/+1 counters it died with.
- LithoformEngineTest: a copy of Felisa, Fang of Silverquill's death trigger counts the counters the creature died with.

Usage: step15-copied-trigger-event-tests.py [repository root, default: this script's repository]

Idempotent: each test is added only when its method name is absent, each import and @CardUsed entry only once.
"""
from pathlib import Path
import sys

ROOT = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else Path(__file__).resolve().parent.parent
TEST_ROOT = ROOT / "magical-vibes-card-test"


def add_import(text, line):
    if line in text:
        return text
    lines = text.split("\n")
    imports = [i for i, l in enumerate(lines) if l.startswith("import com.github.")]
    position = next((i for i in imports if lines[i] > line), imports[-1] + 1)
    lines.insert(position, line)
    return "\n".join(lines)


def add_card_used(text, card_class):
    start = text.index("@CardUsed({")
    end = text.index("})", start)
    if f"{card_class}.class" in text[start:end]:
        return text
    last_line = text[text.rindex("\n", 0, end) + 1:end]
    separator = ",\n        " if len(last_line) + len(card_class) + 10 > 120 else ", "
    return text[:end] + separator + f"{card_class}.class" + text[end:]


def add_test(text, method_name, body):
    if f"void {method_name}()" in text:
        return text
    anchor = text.rindex("\n    private ") if "\n    private " in text else text.rindex("\n}")
    return text[:anchor] + body + text[anchor:]


def edit(path, imports, card_classes, method_name, body):
    file = TEST_ROOT / path
    text = file.read_text()
    original = text
    for line in imports:
        text = add_import(text, line)
    for card_class in card_classes:
        text = add_card_used(text, card_class)
    text = add_test(text, method_name, body)
    if text != original:
        file.write_text(text)
        print(f"updated {path}")
    else:
        print(f"unchanged {path}")


STRIONIC_TEST = '''
    @Test
    @DisplayName("A copied death trigger counts the counters the creature died with")
    void copiedDeathTriggerReadsTheCreatureThatDied() {
        addReadyResonator();
        Permanent walker = addCreatureReady(player1, new HangarbackWalker());
        walker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        walker.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, findBattlefieldIndex(player1, "Strionic Resonator"), null,
                gd.stack.getFirst().getTargetableId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Thopter")).hasSize(2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }
'''

LITHOFORM_TEST = '''
    @Test
    @DisplayName("A copied death trigger counts the counters the creature died with")
    void copiedDeathTriggerCountsTheCountersTheCreatureDiedWith() {
        addReadyEngine();
        harness.addToBattlefield(player1, new FelisaFangOfSilverquill());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        bears.setMarkedDamage(4);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, 0, 0, null, gd.stack.getFirst().getTargetableId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Inkling")).hasSize(2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Inkling")).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }
'''


def main():
    edit("s/src/test/java/com/github/laxika/magicalvibes/cards/s/StrionicResonatorTest.java",
         ["import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;",
          "import com.github.laxika.magicalvibes.model.CounterType;"],
         ["HangarbackWalker"], "copiedDeathTriggerReadsTheCreatureThatDied", STRIONIC_TEST)
    edit("l/src/test/java/com/github/laxika/magicalvibes/cards/l/LithoformEngineTest.java",
         ["import com.github.laxika.magicalvibes.cards.f.FelisaFangOfSilverquill;",
          "import com.github.laxika.magicalvibes.model.CounterType;"],
         ["FelisaFangOfSilverquill"], "copiedDeathTriggerCountsTheCountersTheCreatureDiedWith", LITHOFORM_TEST)
    return 0


if __name__ == "__main__":
    sys.exit(main())
