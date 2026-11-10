#!/usr/bin/env python3
"""Step 15, run 17z13e2: the card cases of the run's rules fixes.

- `RainbowValeTest.anotherManaAbilityGivesNoControlChange`: Rainbow Vale's "an opponent gains control of this land at
  the beginning of the next end step" is part of its own mana ability; written as a land-tap trigger it fired for any
  mana ability of the land (Urborg, Tomb of Yawgmoth's granted Swamp ability).
- `CagedSunTest.departedLandsManaGetsBonus`: Caged Sun's bonus for a land that left as its mana ability resolved
  (Gemstone Mine's last use), read from the tapped land as it last existed; the handler looked for it on the battlefield.

Idempotent. Usage: step15-run17z13e2-tests.py [LETTER...]  (run per letter module: c r)
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "com.github.laxika.magicalvibes"


def add_import(text, fqcn):
    line = f"import {fqcn};\n"
    if line in text:
        return text
    lines = text.splitlines(keepends=True)
    imports = [i for i, l in enumerate(lines) if l.startswith("import ") and not l.startswith("import static")]
    after = [i for i in imports if lines[i] < line and lines[i].split(".")[0] == line.split(".")[0]]
    at = (after[-1] + 1) if after else imports[0]
    lines.insert(at, line)
    return "".join(lines)


def add_case(path, marker, case, imports, card_used):
    text = path.read_text()
    if marker in text:
        return False
    for fqcn in imports:
        text = add_import(text, fqcn)
    for old, new in card_used:
        text = text.replace(old, new, 1)
    end = text.rstrip().rindex("}")
    text = text[:end].rstrip() + "\n\n" + case + "}\n"
    path.write_text(text)
    return True


RAINBOW_VALE = '''    @Test
    @DisplayName("Tapping it for mana with another mana ability gives no control change: that is its own ability's")
    void anotherManaAbilityGivesNoControlChange() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RainbowVale());
        harness.addToBattlefield(player1, new UrborgTombOfYawgmoth());

        // Urborg makes Rainbow Vale a Swamp: its second mana ability is "{T}: Add {B}."
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.CLEANUP, harness::passBothPriorities);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
    }
'''

CAGED_SUN = '''    @Test
    @DisplayName("Gemstone Mine's last use producing the chosen color still adds one extra mana")
    void departedLandsManaGetsBonus() {
        Permanent sun = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        sun.setChosenColor(CardColor.GREEN);
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GemstoneMine());
        mine.setCounterCount(CounterType.MINING, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.assertNotOnBattlefield(player1, "Gemstone Mine");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
'''

CASES = {
    "r": [(TESTS / "r/src/test/java/com/github/laxika/magicalvibes/cards/r/RainbowValeTest.java",
           "anotherManaAbilityGivesNoControlChange", RAINBOW_VALE,
           [f"{PKG}.cards.u.UrborgTombOfYawgmoth"],
           [("@CardUsed({RainbowVale.class, RuinsOfTrokair.class})",
             "@CardUsed({RainbowVale.class, RuinsOfTrokair.class, UrborgTombOfYawgmoth.class})")])],
    "c": [(TESTS / "c/src/test/java/com/github/laxika/magicalvibes/cards/c/CagedSunTest.java",
           "departedLandsManaGetsBonus", CAGED_SUN,
           [f"{PKG}.cards.g.GemstoneMine", f"{PKG}.model.CounterType"],
           [("@CardUsed({CagedSun.class, Forest.class,", "@CardUsed({CagedSun.class, Forest.class, GemstoneMine.class,")])],
}


def main():
    letters = sys.argv[1:] or sorted(CASES)
    for letter in letters:
        for path, marker, case, imports, card_used in CASES.get(letter, []):
            print(f"{path.name}: {'added' if add_case(path, marker, case, imports, card_used) else 'already there'}")


if __name__ == "__main__":
    main()
