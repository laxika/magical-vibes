#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 9: card tests for "whenever you attack with one or more ..." as a trigger filter.

Idempotent; run once per letter test module:

    for m in magical-vibes-card-test/*/; do python3 scripts/step15-event-permanents-tests.py "$m"; done

Witch-king, Sky Scourge had one test. Its trigger is now Triggers.youAttack().withFilter(Wraith) and its X
TotalPowerOfControlledCreatures.declaredAttackers(null), so two cases are added: the ability doesn't trigger when no
Wraith attacks (CR 603.2), and a Wraith that left the battlefield counts with its last-known power (the card's official
ruling, CR 608.2h).
"""
import pathlib
import re
import sys

TESTS = {
    "WitchKingSkyScourgeTest.java": {
        "marker": "doesNotTriggerWhenNoWraithAttacks",
        "imports": ["com.github.laxika.magicalvibes.model.TurnStep"],
        "methods": '''
    @Test
    @DisplayName("Doesn't trigger when no Wraith attacks")
    void doesNotTriggerWhenNoWraithAttacks() {
        addCreatureReady(player1, new WitchKingSkyScourge());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        List<Card> library = new ArrayList<>(List.of(new Mountain(), new Mountain()));
        harness.setLibrary(player1, library);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () ->
                declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears))));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Wraith that left the battlefield counts with its last-known power")
    void wraithThatLeftCountsWithItsLastKnownPower() {
        Permanent witchKing = addCreatureReady(player1, new WitchKingSkyScourge());
        Permanent wraith = addCreatureReady(player1, new LordOfTheNazgL());

        int attackingWraithPower = gqs.getEffectivePower(gd, witchKing) + gqs.getEffectivePower(gd, wraith);
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < attackingWraithPower + 2; i++) {
            library.add(new Mountain());
        }
        harness.setLibrary(player1, library);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(
                    gd.playerBattlefields.get(player1.getId()).indexOf(witchKing),
                    gd.playerBattlefields.get(player1.getId()).indexOf(wraith)));
            gd.playerBattlefields.get(player1.getId()).remove(wraith);
            harness.passBothPriorities();
        });

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyElementsOf(library.subList(0, attackingWraithPower));
    }
''',
    },
}


def fix_imports(src, add):
    """Adds each missing import after the project import that sorts just before it, leaving the others in place."""
    for name in add:
        line = f"import {name};\n"
        if line in src:
            continue
        project = re.findall(r"^import com\.github\.[^;]+;\n", src, flags=re.M)
        before = [existing for existing in project if existing < line]
        anchor = before[-1] if before else project[0]
        at = src.index(anchor) + (len(anchor) if before else 0)
        src = src[:at] + line + src[at:]
    return src


def convert(path, spec):
    src = path.read_text()
    if spec["marker"] in src:
        return
    body_end = src.rstrip().rindex("}")
    converted = src[:body_end].rstrip("\n") + "\n" + spec["methods"] + "}\n"
    converted = fix_imports(converted, spec["imports"])
    path.write_text(converted)
    print(f"converted {path}")


def main(module):
    for name, spec in TESTS.items():
        for path in sorted(pathlib.Path(module).rglob(name)):
            if "/build/" not in str(path):
                convert(path, spec)


if __name__ == "__main__":
    main(sys.argv[1])
