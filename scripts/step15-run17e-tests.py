#!/usr/bin/env python3
"""Step 15 run 17e of effect-slot-refactor.md: card tests for "that player controls" read from the trigger event.

Idempotent; run per letter module (``--module d``) or with no argument for every module this run touches.

- Dokuchi Silencer: a three-player case (the reflexive target is chosen among the permanents of the player dealt
  combat damage, not another opponent's) and a control-change case (the target is rechecked as it resolves).
- Chandra's Incinerator: a three-player case (only the opponent dealt noncombat damage's permanents are offered).

Both add the ``addThirdPlayer`` helper other multiplayer card tests use.
"""
import argparse
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"
MODEL = "com.github.laxika.magicalvibes.model"

THIRD_PLAYER_FIELD = "\n    private Player player3;\n"

THIRD_PLAYER_HELPER = '''
    private void addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
    }
'''

HELPER_IMPORTS = [
    f"import {MODEL}.ManaPool;\n",
    f"import {MODEL}.Player;\n",
    "import com.github.laxika.magicalvibes.testutil.FakeConnection;\n",
    "import java.util.ArrayList;\n",
    "import java.util.UUID;\n",
]

DOKUCHI_TESTS = '''
    @Test
    @DisplayName("The reflexive ability targets only permanents the player dealt combat damage controls, not another opponent's")
    void targetsOnlyTheDamagedPlayersPermanents() {
        addThirdPlayer();
        Permanent silencer = addCreatureReady(player1, new DokuchiSilencer());
        silencer.setAttacking(true);
        silencer.setAttackTarget(player2.getId());
        Permanent damagedPlayersCreature = addCreatureReady(player2, new BearerOfMemory());
        Permanent otherOpponentsCreature = addCreatureReady(player3, new BearerOfMemory());
        harness.setHand(player1, List.of(new BearerOfMemory()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.PermanentChoice targetChoice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactly(damagedPlayersCreature.getId());
        harness.handlePermanentChosen(player1, damagedPlayersCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bearer of Memory");
        assertThat(gd.playerBattlefields.get(player3.getId())).contains(otherOpponentsCreature);
    }

    @Test
    @DisplayName("The reflexive ability does nothing if its target is controlled by another player as it resolves")
    void targetChangingControllerIsIllegalOnResolution() {
        Permanent silencer = addCreatureReady(player1, new DokuchiSilencer());
        silencer.setAttacking(true);
        Permanent target = addCreatureReady(player2, new BearerOfMemory());
        harness.setHand(player1, List.of(new BearerOfMemory()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }
'''

CHANDRA_TESTS = '''
    @Test
    @DisplayName("Targets only permanents the opponent dealt noncombat damage controls, not another opponent's")
    void targetsOnlyTheDamagedOpponentsPermanents() {
        addThirdPlayer();
        harness.addToBattlefield(player1, new ChandrasIncinerator());
        Permanent damagedOpponentsCreature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        Permanent otherOpponentsCreature = harness.addToBattlefieldAndReturn(player3, new AlpineWatchdog());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(damagedOpponentsCreature.getId());
        harness.handlePermanentChosen(player1, damagedOpponentsCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(damagedOpponentsCreature);
        assertThat(otherOpponentsCreature.getMarkedDamage()).isZero();
    }
'''

CASES = {
    ("d", "DokuchiSilencerTest"): ("void targetsOnlyTheDamagedPlayersPermanents()", DOKUCHI_TESTS,
                                   "class DokuchiSilencerTest extends BaseCardTest {\n"),
    ("c", "ChandrasIncineratorTest"): ("void targetsOnlyTheDamagedOpponentsPermanents()", CHANDRA_TESTS,
                                       "class ChandrasIncineratorTest extends BaseCardTest {\n"),
}


def add_import(text, line):
    if line in text:
        return text
    anchor = "import org.junit.jupiter.api.DisplayName;\n"
    if anchor not in text:
        sys.exit(f"no import anchor for {line!r}")
    return text.replace(anchor, line + anchor, 1)


def convert(letter, name, marker, tests, class_line):
    path = TESTS / letter / PKG / letter / f"{name}.java"
    text = path.read_text()
    if marker in text:
        print(f"unchanged {path.relative_to(ROOT)}")
        return
    if text.count(class_line) != 1:
        sys.exit(f"{path}: expected one class declaration")
    text = text.replace(class_line, class_line + THIRD_PLAYER_FIELD, 1)
    body_end = text.rstrip().rfind("}")
    text = text[:body_end].rstrip("\n") + "\n" + tests + THIRD_PLAYER_HELPER + "}\n"
    for line in HELPER_IMPORTS:
        text = add_import(text, line)
    path.write_text(text)
    print(f"converted {path.relative_to(ROOT)}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--module", help="letter module to convert (default: all this run touches)")
    args = parser.parse_args()
    for (letter, name), (marker, tests, class_line) in CASES.items():
        if args.module is None or args.module == letter:
            convert(letter, name, marker, tests, class_line)


if __name__ == "__main__":
    main()
