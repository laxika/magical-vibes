#!/usr/bin/env python3
"""Step 15, run 17x2: the damage family's new card test cases.

Adds the rules-fix cases for "that creature's controller" / "that creature's or spell's controller" read as the
ability resolves (CR 608.2h), each changing the creature's controller with Act of Aggression while the ability waits:

- `GreatbowDoyenTest#damagedCreaturesControllerAsTheAbilityResolvesIsDealtTheDamage`
- `JusticeTest#sourcesControllerAsTheAbilityResolvesIsDealtTheDamage`
- `FlayedNimTest#damagedCreaturesControllerAsTheAbilityResolvesLosesTheLife`

Idempotent: a test already holding its case is left alone. Per letter test module.

Usage: step15-run17x2-tests.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ACT = "import com.github.laxika.magicalvibes.cards.a.ActOfAggression;"
TURN_STEP = "import com.github.laxika.magicalvibes.model.TurnStep;"

TESTS = {
    "GreatbowDoyenTest": ("g", "damagedCreaturesControllerAsTheAbilityResolvesIsDealtTheDamage",
                          "@CardUsed({GreatbowDoyen.class, ", "@CardUsed({GreatbowDoyen.class, ActOfAggression.class, ",
                          """    @Test
    @DisplayName("The damaged creature's controller as the ability resolves is dealt the damage")
    void damagedCreaturesControllerAsTheAbilityResolvesIsDealtTheDamage() {
        addCreatureReady(player1, new GreatbowDoyen());
        Permanent archer = addCreatureReady(player1, new KeenEyedArchers());
        archer.setAttacking(true);
        // A 4/4 blocker survives the boosted 3/3 Archer's damage, so its controller can change in response.
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);

        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveInstant(player1, 0, blocker.getId());
        harness.assertOnBattlefield(player1, "Serra Angel");
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

"""),
    "JusticeTest": ("j", "sourcesControllerAsTheAbilityResolvesIsDealtTheDamage",
                    "@CardUsed({Justice.class, ", "@CardUsed({Justice.class, ActOfAggression.class, ",
                    """    @Test
    @DisplayName("The red creature's controller as the ability resolves is dealt the damage")
    void sourcesControllerAsTheAbilityResolvesIsDealtTheDamage() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBarbarians());
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        attacker.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);

        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new ActOfAggression()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        harness.assertOnBattlefield(player2, "Balduvian Barbarians");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }

"""),
    "FlayedNimTest": ("f", "damagedCreaturesControllerAsTheAbilityResolvesLosesTheLife",
                      "@CardUsed({FlayedNim.class, ", "@CardUsed({FlayedNim.class, ActOfAggression.class, ",
                      """    @Test
    @DisplayName("The damaged creature's controller as the ability resolves loses the life")
    void damagedCreaturesControllerAsTheAbilityResolvesLosesTheLife() {
        Permanent nim = addCreatureReady(player1, new FlayedNim());
        nim.setAttacking(true);
        // A 0/4 blocker survives the Nim's 2 damage, so its controller can change in response.
        Permanent blocker = addCreatureReady(player2, new SteelWall());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);

        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveInstant(player1, 0, blocker.getId());
        harness.assertOnBattlefield(player1, "Steel Wall");
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

"""),
}


def convert(name, letter, method, used_old, used_new, case):
    path = (ROOT / f"magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/{letter}"
            / f"{name}.java")
    text = path.read_text()
    if f"void {method}()" in text:
        return
    if used_old not in text:
        raise SystemExit(f"{name}: @CardUsed not found")
    text = text.replace(used_old, used_new, 1)
    if ACT not in text:
        lines = text.split("\n")
        imports = [i for i, l in enumerate(lines) if l.startswith("import com.github.laxika.magicalvibes.cards.")]
        position = next((i for i in imports if lines[i] > ACT), imports[-1] + 1)
        lines.insert(position, ACT)
        text = "\n".join(lines)
    if TURN_STEP not in text:
        lines = text.split("\n")
        imports = [i for i, l in enumerate(lines) if l.startswith("import com.github.laxika.magicalvibes.model.")]
        position = next((i for i in imports if lines[i] > TURN_STEP), imports[-1] + 1)
        lines.insert(position, TURN_STEP)
        text = "\n".join(lines)
    anchor = "    @Test\n"
    index = text.index(anchor)
    text = text[:index] + case + text[index:]
    path.write_text(text)
    print(f"added {name}#{method}")


def main():
    letters = set("abcdefghijklmnopqrstuvwxyz") if sys.argv[1:] == ["all"] else set(sys.argv[1:])
    if not letters:
        raise SystemExit(__doc__)
    for name, (letter, method, used_old, used_new, case) in TESTS.items():
        if letter in letters:
            convert(name, letter, method, used_old, used_new, case)


if __name__ == "__main__":
    main()
