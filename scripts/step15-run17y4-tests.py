#!/usr/bin/env python3
"""Step 15, run 17y4: the card tests of the rules fix and of the new recipient filter.

- Lolth, Spider Queen: the emblem's "if that player lost less than 8 life this turn, they lose life equal to the
  difference" counted every opponent's life lost and made every opponent lose life (`LifeLostThisTurn(OPPONENTS)`,
  `EACH_OPPONENT`), right only with one opponent; "that player" is the opponent who was dealt the combat damage.
- Sower of Discord: "whenever damage is dealt to one of the chosen players" is the trigger's damaged-player filter;
  damage to a player who wasn't chosen doesn't trigger it (a guard of the filter, which passes at HEAD too).

Idempotent: a test already added is left alone. Optional repository-root argument (for the HEAD worktree).

Usage: step15-run17y4-tests.py [<repository root>]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = "magical-vibes-card-test/{0}/src/test/java/com/github/laxika/magicalvibes/cards/{0}/{1}.java"

THIRD_PLAYER = '''    private Player addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }

'''

LOLTH = '''    @Test
    @DisplayName("With two opponents the emblem reads only the damaged opponent's life lost and only they lose life")
    void emblemReadsOnlyTheDamagedOpponentsLifeLost() {
        Player player3 = addThirdPlayer();
        addReadyLolth(player1, 8);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player3.getId());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.getLife(player3.getId())).isEqualTo(18);
    }

''' + THIRD_PLAYER

SOWER = '''    @Test
    void damageToAPlayerWhoWasNotChosenDoesNotTriggerLifeLoss() {
        Player player3 = addThirdPlayer();
        castSowerOfDiscord();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player3.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertLife(player3, 18);
    }

''' + THIRD_PLAYER

COMMON_IMPORTS = [
    ("import com.github.laxika.magicalvibes.model.ManaPool;", "import com.github.laxika.magicalvibes.model.ManaColor;"),
    ("import com.github.laxika.magicalvibes.testutil.FakeConnection;",
     "import com.github.laxika.magicalvibes.testutil.CardUsed;"),
    ("import java.util.ArrayList;", None),
    ("import java.util.UUID;", "import java.util.List;"),
]

ADDITIONS = [
    ("l", "LolthSpiderQueenTest", "emblemReadsOnlyTheDamagedOpponentsLifeLost", LOLTH,
     "    private Permanent addReadyLolth(Player player, int loyalty) {", COMMON_IMPORTS),
    ("s", "SowerOfDiscordTest", "damageToAPlayerWhoWasNotChosenDoesNotTriggerLifeLoss", SOWER,
     "    private Permanent castSowerOfDiscord() {",
     [("import com.github.laxika.magicalvibes.model.Player;", "import com.github.laxika.magicalvibes.model.Permanent;")]
     + COMMON_IMPORTS),
]


def main():
    for letter, name, method, body, anchor, imports in ADDITIONS:
        path = ROOT / TESTS.format(letter, name)
        text = path.read_text()
        if method in text:
            continue
        if anchor not in text:
            raise SystemExit(f"{name}: anchor not found")
        text = text.replace(anchor, body + anchor, 1)
        for line, after in imports:
            if line in text:
                continue
            if after is None:
                java = text.index("\nimport java.") + 1
                text = text[:java] + line + "\n" + text[java:]
            else:
                if after not in text:
                    raise SystemExit(f"{name}: import anchor {after} not found")
                text = text.replace(after, after + "\n" + line, 1)
        path.write_text(text)
        print(f"added {name}#{method}")


if __name__ == "__main__":
    main()
