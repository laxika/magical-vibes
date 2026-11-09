#!/usr/bin/env python3
"""Step 15, run 17z11f6: the new card test cases. Idempotent; optional repository-root argument (the HEAD worktree)."""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
CARD_TESTS = ROOT / "magical-vibes-card-test"


def test(letter, name):
    return CARD_TESTS / letter / "src/test/java/com/github/laxika/magicalvibes/cards" / letter / f"{name}.java"


def add_case(path, case, anchor_method, imports=(), pairs=()):
    s = path.read_text()
    if anchor_method in s:
        return
    s = s.rstrip()
    assert s.endswith("}")
    s = s[:-1].rstrip() + "\n" + case + "}\n"
    for imp in imports:
        line = f"import {imp};\n"
        if line in s:
            continue
        if imp.startswith("static "):
            last = s.rindex("\nimport ")
            end = s.index("\n", last + 1) + 1
            s = s[:end] + "\n" + line + s[end:]
        else:
            first = s.index("import ")
            s = s[:first] + line + s[first:]
    for old, new in pairs:
        if old not in s:
            raise SystemExit(f"{path.name}: missing {old!r}")
        s = s.replace(old, new)
    path.write_text(s)
    print("edited", path.name)


# Righteous Cause turned into a Forest by Song of the Dryads has no "whenever a creature attacks" ability: the
# any-creature attack collector read the printed abilities of every watcher, also one that lost them (a rules fix).
RIGHTEOUS = '''
    @Test
    @DisplayName("Righteous Cause that lost its abilities to Song of the Dryads gains no life")
    void lostAbilitiesDoNotTrigger() {
        Permanent cause = harness.addToBattlefieldAndReturn(player1, new RighteousCause());
        Permanent song = harness.addToBattlefieldAndReturn(player2, new SongOfTheDryads());
        song.setAttachedTo(cause.getId());
        addCreatureReady(player2, new GlorySeeker());
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(1));
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Righteous Cause"));
        resolveAllTriggers();

        harness.assertLife(player1, 18); // the Glory Seeker's 2 combat damage, and no life gained
    }
'''

# Balthier and Fran: "a Vehicle crewed by Balthier and Fran this turn" is the trigger's filter, so a Vehicle another
# creature crewed doesn't trigger it.
BALTHIER = '''
    @Test
    @DisplayName("A Vehicle crewed by another creature does not trigger the ability")
    void vehicleCrewedByAnotherCreatureDoesNotTrigger() {
        Permanent balthier = addCreatureReady(player1, new BalthierAndFran());
        balthier.tap();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
            harness.passBothPriorities();
        });
        vehicle.untap();

        declareAttackers(player1, List.of(indexOf(player1, vehicle)), 1);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Balthier and Fran"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
'''


# Apple of Eden, Isu Relic: "its owner draws a card" reads the played card's owner; the test's hand cards are owned by
# the player whose hand they are in (CR 400.3), as every card of a real game is.
APPLE_HELPER = '''
    /** {@code cards}, owned by {@code owner}, as the cards in a player's hand are (CR 400.3). */
    private static List<Card> ownedBy(Player owner, Card... cards) {
        for (Card card : cards) {
            card.setOwnerId(owner.getId());
        }
        return List.of(cards);
    }
'''


def apple():
    path = test("a", "AppleOfEdenIsuRelicTest")
    s = path.read_text()
    if "ownedBy(Player owner" in s:
        return
    s = re.sub(r"harness\.setHand\(player2, List\.of\((?=[^)])", "harness.setHand(player2, ownedBy(player2, ", s)
    s = s.rstrip()
    assert s.endswith("}")
    s = s[:-1].rstrip() + "\n" + APPLE_HELPER + "}\n"
    for imp in ("com.github.laxika.magicalvibes.model.Card", "com.github.laxika.magicalvibes.model.Player"):
        line = f"import {imp};\n"
        if line not in s:
            first = s.index("import ")
            s = s[:first] + line + s[first:]
    path.write_text(s)
    print("edited", path.name)


def main():
    add_case(test("r", "RighteousCauseTest"), RIGHTEOUS, "lostAbilitiesDoNotTrigger",
             ["com.github.laxika.magicalvibes.cards.s.SongOfTheDryads",
              "com.github.laxika.magicalvibes.model.Permanent",
              "static org.assertj.core.api.Assertions.assertThat"],
             [("@CardUsed({RighteousCause.class, GlorySeeker.class})",
               "@CardUsed({RighteousCause.class, GlorySeeker.class, SongOfTheDryads.class})")])
    add_case(test("b", "BalthierAndFranTest"), BALTHIER, "vehicleCrewedByAnotherCreatureDoesNotTrigger")
    apple()


if __name__ == "__main__":
    main()
