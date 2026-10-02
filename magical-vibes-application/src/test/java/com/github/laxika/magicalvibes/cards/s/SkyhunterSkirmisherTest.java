package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SkyhunterSkirmisher.class, GrizzlyBears.class, BenalishKnight.class})
class SkyhunterSkirmisherTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike deals damage twice to a blocker, totaling double power")
    void doubleStrikeDealsDamageTwiceToBlocker() {
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Skyhunter Skirmisher");
    }

    @Test
    @DisplayName("Double strike kills 1/1 blocker in first strike phase, Skirmisher survives")
    void doubleStrikeKillsSmallBlockerInFirstStrikePhase() {
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        Permanent blocker = addCreatureReady(player2, smallCreature);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Skyhunter Skirmisher");
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

        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(3);
        bigCreature.setToughness(3);
        Permanent blocker = addCreatureReady(player2, bigCreature);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Skyhunter Skirmisher");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Double strike trades with equal-power first strike creature")
    void doubleStrikeTradesWithFirstStrike() {
        Permanent attacker = addReadySkirmisher(player1);
        attacker.setAttacking(true);

        BenalishKnight firstStrikeCreature = new BenalishKnight();
        firstStrikeCreature.setPower(1);
        firstStrikeCreature.setToughness(1);
        Permanent blocker = addCreatureReady(player2, firstStrikeCreature);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Skyhunter Skirmisher");
        harness.assertNotOnBattlefield(player2, "Benalish Knight");
    }

    private Permanent addReadySkirmisher(Player player) {
        return addCreatureReady(player, new SkyhunterSkirmisher());
    }
}
