#!/usr/bin/env python3
"""Step 15, run 17w3: the card test of the run's rules fix.

Cephalid Shrine's ability triggers on a spell that can't be countered (CR 603.2): the caster is offered the payment
(CR 118.12a) and the spell resolves whether or not they pay. The old test asserted the class-keyed handler's skip of an
uncounterable spell, so it is rewritten to the Oracle behaviour. Idempotent (a marker names the rewritten case). Per
letter module.

Usage: step15-run17w3-tests.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{name}Test.java"

EDITS = {
    ("c", "CephalidShrine", "uncounterableSpellStillTriggersAndResolves"): [
        ("""    @Test
    @DisplayName("An uncounterable spell is not offered a payment to avoid being countered")
    void noPaymentOfferedForUncounterableSpell() {
        harness.addToBattlefield(player1, new CephalidShrine());
        harness.setGraveyard(player1, List.of(new SurrakElusiveHunter()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new SurrakElusiveHunter(), "{2}{G}");
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Surrak, Elusive Hunter");
        harness.assertNotInGraveyard(player2, "Surrak, Elusive Hunter");
    }""",
         """    @Test
    @DisplayName("An uncounterable spell still triggers it, and resolves when its caster doesn't pay")
    void uncounterableSpellStillTriggersAndResolves() {
        harness.addToBattlefield(player1, new CephalidShrine());
        harness.setGraveyard(player1, List.of(new SurrakElusiveHunter()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new SurrakElusiveHunter(), "{2}{G}");
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passBothPriorities();

        // The ability triggers whether or not the spell can be countered, and its caster may pay.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Surrak, Elusive Hunter");
        harness.assertNotInGraveyard(player2, "Surrak, Elusive Hunter");
    }"""),
    ],
}


def main():
    letters = set("abcdefghijklmnopqrstuvwxyz") if sys.argv[1:] == ["all"] else set(sys.argv[1:])
    if not letters:
        raise SystemExit(__doc__)
    for (letter, name, marker), edits in EDITS.items():
        if letter not in letters:
            continue
        path = pathlib.Path(str(TESTS).format(letter=letter, name=name))
        text = path.read_text()
        if marker in text:
            continue
        for old, new in edits:
            if text.count(old) != 1:
                raise SystemExit(f"{name}: expected text not found once: {old!r}")
            text = text.replace(old, new)
        path.write_text(text)
        print(f"updated {name}")


if __name__ == "__main__":
    main()
