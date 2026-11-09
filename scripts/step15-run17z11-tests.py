#!/usr/bin/env python3
"""Step 15, run 17z11: the card tests of the four multiplayer "opponent" picks the trigger collectors made with
`getOpponentId`.

- `ForbiddenOrchardTest`: "target opponent" is chosen as the ability goes on the stack; every case answers the target
  prompt, and the new `#controllerChoosesWhichOpponentCreatesTheSpirit` offers both opponents, not the controller, in a
  three-player game.
- `TreasureNabberTest#triggersWhenAnyOpponentTapsAnArtifactForMana`: "an opponent" is any opponent, here the third
  player.
- `ZidaneTantalusThiefTest#anyOpponentGainingControlCreatesTreasure`: "an opponent gains control of a permanent from
  you" is any opponent, here the third player (the control change collected directly).
- `KarplusanMinotaurTest#lostFlipControllerChoosesTheOpponentWhoChoosesTheTarget`: "any target of an opponent's
  choice" in a three-player game: the controller chooses the opponent, who chooses the target.

The new cases fail at HEAD `ea499bb104`. Idempotent; optional repository-root argument (for the HEAD worktree).

Usage: step15-run17z11-tests.py [<repository root>]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "com/github/laxika/magicalvibes/cards"

THIRD_PLAYER = '''
    private Player addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        Player player3 = new Player(thirdPlayerId, "Charlie");
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
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
        return player3;
    }
'''
THIRD_PLAYER_IMPORTS = [
    "import com.github.laxika.magicalvibes.model.ManaPool;",
    "import com.github.laxika.magicalvibes.model.Player;",
    "import com.github.laxika.magicalvibes.testutil.FakeConnection;",
    "import java.util.ArrayList;",
    "import java.util.UUID;",
]


def add_imports(text, imports):
    for line in imports:
        if line not in text:
            anchor = text.index("\nimport ") + 1
            text = text[:anchor] + line + "\n" + text[anchor:]
    return text


def add_before_last_brace(text, block):
    end = text.rstrip().rindex("}")
    return text[:end].rstrip("\n") + "\n" + block + "}\n"


def edit(path, change):
    text = path.read_text()
    new = change(text)
    if new != text:
        path.write_text(new)
        print(f"{path.name}: changed")
    else:
        print(f"{path.name}: already changed")


ORCHARD_CHOICES = [
    ('''        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(before + 1);''',
     '''        harness.handleListChoice(player1, "BLUE");
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(before + 1);'''),
    ('''        harness.handleListChoice(player1, "GREEN");
        harness.passBothPriorities();''',
     '''        harness.handleListChoice(player1, "GREEN");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();'''),
    ('''        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();''',
     '''        harness.handleListChoice(player1, "RED");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();'''),
    ('''        harness.handleListChoice(player1, "WHITE");
        harness.passBothPriorities();''',
     '''        harness.handleListChoice(player1, "WHITE");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();'''),
    ('''        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools''',
     '''        harness.handleListChoice(player1, "BLACK");
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerManaPools'''),
    ('''        harness.handleListChoice(player2, "BLUE");
        harness.passBothPriorities();''',
     '''        harness.handleListChoice(player2, "BLUE");
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();'''),
]

ORCHARD_CASE = '''
    @Test
    @DisplayName("The controller chooses which opponent creates the Spirit; the controller is not a legal target")
    void controllerChoosesWhichOpponentCreatesTheSpirit() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player1, new ForbiddenOrchard());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player2.getId(), player3.getId());
        harness.handlePermanentChosen(player1, player3.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player3, "Spirit")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Spirit");
        harness.assertNotOnBattlefield(player1, "Spirit");
    }
'''

NABBER_CASE = '''
    @Test
    void triggersWhenAnyOpponentTapsAnArtifactForMana() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player1, new TreasureNabber());
        Permanent solRing = harness.addToBattlefieldAndReturn(player3, new SolRing());

        harness.activateAbility(player3, 0, null, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sol Ring");
        harness.assertNotOnBattlefield(player3, "Sol Ring");
        assertThat(solRing.isTapped()).isTrue();
    }
'''

MINOTAUR_CASE = '''
    @Test
    @DisplayName("A lost flip's target is chosen by the opponent its controller chooses")
    void lostFlipControllerChoosesTheOpponentWhoChoosesTheTarget() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player1, new KarplusanMinotaur());

        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkControllerLosesCoinFlipTriggers(gd, player1.getId());
            harness.getTriggerCollectionService().processNextSpellTargetTrigger(gd);
        });

        PendingInteraction.PermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentChoice.playerId()).isEqualTo(player1.getId());
        assertThat(opponentChoice.validPlayerIds()).containsExactlyInAnyOrder(player2.getId(), player3.getId());
        harness.handlePermanentChosen(player1, player3.getId());

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.playerId()).isEqualTo(player3.getId());
        harness.handlePermanentChosen(player3, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.assertLife(player3, 20);
    }
'''


ZIDANE_CASE = '''
    @Test
    @DisplayName("Creates a Treasure when any opponent gains control of a permanent from you")
    void anyOpponentGainingControlCreatesTreasure() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player1, new ZidaneTantalusThief());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkOpponentGainsControlTriggers(gd, target, player1.getId(), player3.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }
'''


def orchard(text):
    for old, new in ORCHARD_CHOICES:
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"ForbiddenOrchardTest: the expected text is missing: {old.strip()[:80]}")
        text = text.replace(old, new)
    return with_case(text, "controllerChoosesWhichOpponentCreatesTheSpirit", ORCHARD_CASE)


def with_case(text, name, case):
    if f"void {name}()" in text:
        return text
    text = add_imports(text, THIRD_PLAYER_IMPORTS)
    if "private Player addThirdPlayer()" not in text:
        text = add_before_last_brace(text, THIRD_PLAYER)
    return add_before_last_brace(text, case)


def main():
    edit(TESTS / f"f/src/test/java/{PKG}/f/ForbiddenOrchardTest.java", orchard)
    edit(TESTS / f"t/src/test/java/{PKG}/t/TreasureNabberTest.java",
         lambda text: with_case(text, "triggersWhenAnyOpponentTapsAnArtifactForMana", NABBER_CASE))
    edit(TESTS / f"z/src/test/java/{PKG}/z/ZidaneTantalusThiefTest.java",
         lambda text: with_case(text, "anyOpponentGainingControlCreatesTreasure", ZIDANE_CASE))
    edit(TESTS / f"k/src/test/java/{PKG}/k/KarplusanMinotaurTest.java",
         lambda text: with_case(text, "lostFlipControllerChoosesTheOpponentWhoChoosesTheTarget", MINOTAUR_CASE))


if __name__ == "__main__":
    main()
