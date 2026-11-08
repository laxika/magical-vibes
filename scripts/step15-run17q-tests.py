#!/usr/bin/env python3
"""Step 15, run 17q: Scriv's Contract's "if it's attacking one of your opponents" is read as the ability resolves.

The Contract token's "whenever enchanted creature attacks" is the token's own ability, controlled by the token's
controller (CR 113.7, 113.8), and its "it gets +2/+0 until end of turn if it's attacking one of your opponents.
Otherwise, its controller loses 2 life" is an effect's "if", checked as the ability resolves: the attack collectors no
longer pick the branch as the ability triggers. Adds two three-player cases to `ScrivTheObligatorTest`: the creature
attacking another opponent of the token's controller gets +2/+0, and one removed from combat (Reconnaissance) before
the ability resolves makes its controller lose 2 life. Idempotent.

Usage: step15-run17q-tests.py <letter test module dir>...
"""
import pathlib
import sys

MARKER = "contractBoostsCreatureAttackingAnotherOpponentOfAuraController"

TESTS = '''
    @Test
    void contractBoostsCreatureAttackingAnotherOpponentOfAuraController() {
        Player player3 = addThirdPlayer();
        Permanent target = addCreatureReady(player2, new ZulaportCutthroat());
        castScriv(target);
        harness.setLife(player2, 20);

        declareAttackersAt(player2, target, player3.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @CardUsed(Reconnaissance.class)
    void contractReadsTheAttackAsItResolves() {
        Player player3 = addThirdPlayer();
        Permanent target = addCreatureReady(player2, new ZulaportCutthroat());
        harness.addToBattlefield(player2, new Reconnaissance());
        castScriv(target);
        harness.setLife(player2, 20);

        declareAttackersAt(player2, target, player3.getId());
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).size() - 1, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isAttacking()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

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

IMPORTS = [
    "import com.github.laxika.magicalvibes.cards.r.Reconnaissance;",
    "import com.github.laxika.magicalvibes.model.ManaPool;",
    "import com.github.laxika.magicalvibes.model.Player;",
    "import com.github.laxika.magicalvibes.testutil.FakeConnection;",
    "import java.util.ArrayList;",
    "import java.util.UUID;",
]


def main():
    changed = []
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/test/java/**/ScrivTheObligatorTest.java")):
            source = path.read_text()
            if MARKER in source:
                continue
            anchor = "    private void castScriv(Permanent target) {"
            if source.count(anchor) != 1:
                raise SystemExit(f"anchor not found in {path}")
            source = source.replace(anchor, TESTS.lstrip("\n") + "\n" + anchor)
            missing = [line for line in IMPORTS if line not in source]
            first_import = source.index("import ")
            source = source[:first_import] + "".join(line + "\n" for line in missing) + source[first_import:]
            path.write_text(source)
            changed.append(path.stem)
    print(f"{len(changed)} test classes changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
