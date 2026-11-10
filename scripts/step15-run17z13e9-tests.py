#!/usr/bin/env python3
"""Step 15, run 17z13e9: the card cases of the rules fix.

The combat damage step put "whenever this is dealt damage" abilities on the stack with its own branches, outside the
collectors' dispatch, so an effect that makes a triggered ability trigger an additional time (Roaming Throne) didn't
apply to combat damage; noncombat damage went through the dispatch. `RoamingThroneTest` gains a combat case (fails at
the run's HEAD) and a noncombat one (passes there too) with Ripjaw Raptor (a Dinosaur: "Enrage — Whenever this
creature is dealt damage, draw a card"). Run per letter module: `r`. Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"

CASES = '''
    @Test
    @DisplayName("Roaming Throne doubles a chosen-type creature's \\"whenever this is dealt damage\\" for noncombat damage")
    void doublesDealtDamageTriggerForNoncombatDamage() {
        addThrone(CardSubtype.DINOSAUR);
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new RipjawRaptor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, raptor.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getCard().getName().equals("Ripjaw Raptor")).hasSize(2);
    }

    @Test
    @DisplayName("Roaming Throne doubles a chosen-type creature's \\"whenever this is dealt damage\\" for combat damage")
    void doublesDealtDamageTriggerForCombatDamage() {
        Permanent throne = harness.addToBattlefieldAndReturn(player2, new RoamingThrone());
        throne.setChosenSubtype(CardSubtype.DINOSAUR);
        Permanent raptor = harness.addToBattlefieldAndReturn(player2, new RipjawRaptor());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        raptor.setSummoningSick(false);
        raptor.setBlocking(true);
        raptor.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of());
        harness.passBothPriorities();

        // Combat damage, and the two triggers resolving: a card each.
        harness.assertInGraveyard(player1, "Fugitive Wizard");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }
'''

IMPORTS = [
    "import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;",
    "import com.github.laxika.magicalvibes.cards.s.Shock;",
    "import com.github.laxika.magicalvibes.model.TurnStep;",
]


def main(letters):
    if letters and "r" not in letters:
        return
    path = next(TESTS.glob("*/src/test/java/com/github/laxika/magicalvibes/cards/r/RoamingThroneTest.java"))
    text = path.read_text()
    if "doublesDealtDamageTriggerForNoncombatDamage" in text:
        print("RoamingThroneTest: already done")
        return
    lines = text.split("\n")
    block = [i for i, line in enumerate(lines) if line.startswith("import com.github.laxika.")]
    imports = sorted(set(lines[block[0]:block[-1] + 1]) | set(IMPORTS))
    lines[block[0]:block[-1] + 1] = imports
    text = "\n".join(lines)
    text = text.replace("RatchetFieldMedic.class})", "RatchetFieldMedic.class, RipjawRaptor.class, Shock.class,\n"
                        "        FugitiveWizard.class})", 1)
    assert text.rstrip().endswith("}")
    text = text.rstrip()[:-1].rstrip() + "\n" + CASES + "}\n"
    path.write_text(text)
    print("RoamingThroneTest: 2 cases added")


if __name__ == "__main__":
    main(sys.argv[1:])
