package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.g.GoblinEliteInfantry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorrentOfLava.class, BayFalcon.class, GoblinEliteInfantry.class, Twincast.class})
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

    @Test
    @DisplayName("The caster's creature can prevent damage, without protecting another creature")
    void casterCreaturePreventsOnlyItsOwnDamage() {
        addCreatureReady(player1, new GoblinEliteInfantry());
        harness.addToBattlefield(player2, new GoblinEliteInfantry());
        harness.setHand(player1, List.of(new TorrentOfLava()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Elite Infantry");
        harness.assertNotOnBattlefield(player2, "Goblin Elite Infantry");
    }

    @Test
    @DisplayName("Creatures lose the granted ability once Torrent of Lava has left the stack")
    void grantedAbilityEndsWhenTorrentLeavesStack() {
        addCreatureReady(player2, new GoblinEliteInfantry());
        harness.setHand(player1, List.of(new TorrentOfLava()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability prevents only one damage")
    void preventionDoesNotPreventAllDamage() {
        addCreatureReady(player2, new GoblinEliteInfantry());
        harness.setHand(player1, List.of(new TorrentOfLava()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, 3);
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goblin Elite Infantry");
    }

    @Test
    @DisplayName("A copy of Torrent of Lava grants its own prevention ability, which shields against the copy")
    void copyGrantsPreventionAgainstTheCopy() {
        Permanent goblin = addCreatureReady(player2, new GoblinEliteInfantry());
        StackEntry copy = castTorrentAndCopyIt(1);

        harness.passPriority(player1);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.targetSourceDamagePreventionShields)
                .singleElement()
                .satisfies(shield -> {
                    assertThat(shield.targetId()).isEqualTo(goblin.getId());
                    assertThat(shield.sourceId()).isEqualTo(copy.getCard().getId());
                });

        harness.passBothPriorities();
        assertThat(goblin.getMarkedDamage()).isZero();

        harness.passBothPriorities();
        assertThat(goblin.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Goblin Elite Infantry");
    }

    @Test
    @DisplayName("The original's ability still shields only against the original while a copy is on the stack")
    void originalAbilityDoesNotShieldAgainstTheCopy() {
        Permanent goblin = addCreatureReady(player2, new GoblinEliteInfantry());
        castTorrentAndCopyIt(1);

        harness.passPriority(player1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        harness.passBothPriorities();
        assertThat(goblin.getMarkedDamage()).isEqualTo(1);

        harness.passBothPriorities();
        assertThat(goblin.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Goblin Elite Infantry");
    }

    @Test
    @DisplayName("The copy's granted ability ends once the copy has left the stack")
    void copyAbilityEndsWhenCopyLeavesStack() {
        addCreatureReady(player2, new GoblinEliteInfantry());
        castTorrentAndCopyIt(0);

        harness.passBothPriorities();
        assertThat(gd.stack).singleElement().satisfies(entry -> assertThat(entry.isCopy()).isFalse());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageNotContaining("priority");
        harness.activateAbility(player2, 0, 0, null, null);
        assertThat(gd.stack).hasSize(2);
    }

    private StackEntry castTorrentAndCopyIt(int x) {
        TorrentOfLava torrent = new TorrentOfLava();
        harness.setHand(player1, List.of(torrent));
        harness.addMana(player1, ManaColor.RED, 2 + x);
        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, x);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, torrent.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        StackEntry copy = gd.stack.getLast();
        assertThat(copy.isCopy()).isTrue();
        return copy;
    }
}
