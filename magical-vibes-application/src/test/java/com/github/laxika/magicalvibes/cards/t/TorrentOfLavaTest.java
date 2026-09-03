package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.g.GoblinEliteInfantry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({TorrentOfLava.class, BayFalcon.class, GoblinEliteInfantry.class})
class TorrentOfLavaTest extends BaseCardTest {

    @Test
    @DisplayName("Torrent of Lava kills non-flying creatures on both sides")
    void killsNonFlyingCreatures() {
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        harness.addToBattlefield(player2, new GoblinEliteInfantry());

        harness.setHand(player1, List.of(new TorrentOfLava()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertNotOnBattlefield(player1, "Goblin Elite Infantry");
        harness.assertNotOnBattlefield(player2, "Goblin Elite Infantry");
    }

    @Test
    @DisplayName("Torrent of Lava does not damage flying creatures")
    void doesNotDamageFlyingCreatures() {
        harness.addToBattlefield(player2, new BayFalcon());

        harness.setHand(player1, List.of(new TorrentOfLava()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 3);

        harness.assertOnBattlefield(player2, "Bay Falcon");
    }

    @Test
    @DisplayName("Torrent of Lava deals no damage to players")
    void dealsNoDamageToPlayers() {
        harness.setHand(player1, List.of(new TorrentOfLava()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 3);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Torrent of Lava with X=1 leaves a 2/2 alive")
    void xOneLeavesToughTwoAlive() {
        harness.addToBattlefield(player2, new GoblinEliteInfantry());

        harness.setHand(player1, List.of(new TorrentOfLava()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertOnBattlefield(player2, "Goblin Elite Infantry");
    }

    @Test
    @DisplayName("Torrent of Lava with X=0 deals no damage")
    void xZeroDealsNoDamage() {
        harness.addToBattlefield(player2, new GoblinEliteInfantry());

        harness.setHand(player1, List.of(new TorrentOfLava()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Goblin Elite Infantry");
    }

    @Test
    @DisplayName("A creature can prevent one damage from Torrent of Lava while it is on the stack")
    void creatureCanPreventOneDamageFromTorrentWhileOnStack() {
        addCreatureReady(player2, new GoblinEliteInfantry());

        TorrentOfLava torrent = new TorrentOfLava();
        harness.setHand(player1, List.of(torrent));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 2);
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goblin Elite Infantry");
    }
}
