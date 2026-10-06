package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyhunterSkirmisher.class, GrizzlyBears.class, SuntailHawk.class,
        AirElemental.class, YouthfulKnight.class})
class SkyhunterSkirmisherTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike blocker deals damage twice to an attacker, totaling double power")
    void doubleStrikeDealsDamageTwiceToBlocker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent blocker = addReadySkirmisher(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Skyhunter Skirmisher");
    }

    @Test
    @DisplayName("Double strike kills 1/1 blocker in first strike phase, Skirmisher survives")
    void doubleStrikeKillsSmallBlockerInFirstStrikePhase() {
        harness.setLife(player2, 20);
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertOnBattlefield(player1, "Skyhunter Skirmisher");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A flying creature can legally block Skirmisher and prevents player damage")
    void flyingCreatureCanBlockSkirmisher() {
        harness.setLife(player2, 20);
        addReadySkirmisher(player1);
        addCreatureReady(player2, new SuntailHawk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Suntail Hawk");
        harness.assertOnBattlefield(player1, "Skyhunter Skirmisher");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addReadySkirmisher(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unblocked double strike deals double damage to player")
    void unblockedDoubleStrikeDealsDoubleDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Double strike creature dies to larger blocker that survives both phases")
    void doubleStrikeDiesToLargerBlocker() {
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AirElemental());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Skyhunter Skirmisher");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Double strike blocker trades with a first strike attacker")
    void doubleStrikeTradesWithFirstStrike() {
        Permanent attacker = addCreatureReady(player1, new YouthfulKnight());
        attacker.setAttacking(true);

        Permanent blocker = addReadySkirmisher(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Skyhunter Skirmisher");
        harness.assertNotOnBattlefield(player1, "Youthful Knight");
    }

    private Permanent addReadySkirmisher(Player player) {
        return addCreatureReady(player, new SkyhunterSkirmisher());
    }
}
