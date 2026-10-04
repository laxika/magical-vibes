package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.j.JohtullWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinMutant.class, BalduvianBarbarians.class, BalduvianBears.class, JohtullWurm.class})
class GoblinMutantTest extends BaseCardTest {

    private Permanent mutant() {
        return mutant(player1);
    }

    private Permanent mutant(Player player) {
        return addCreatureReady(player, new GoblinMutant());
    }

    @Test
    @DisplayName("Can attack when the defending player controls no creatures")
    void canAttackWithEmptyDefenderBoard() {
        harness.setLife(player2, 20);
        mutant();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Can attack when the defending player's power 3 creature is tapped")
    void canAttackWhenBigCreatureTapped() {
        harness.setLife(player2, 20);
        Permanent giant = addCreatureReady(player2, new BalduvianBarbarians()); // 3/2
        giant.tap();
        mutant();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Can attack when the defending player's untapped creatures all have power below 3")
    void canAttackWhenOnlySmallUntappedCreatures() {
        harness.setLife(player2, 20);
        addCreatureReady(player2, new BalduvianBears()); // 2/2
        Permanent mutant = mutant();

        declareAttackers(List.of(0));

        assertThat(mutant.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can't attack when the defending player controls an untapped creature with power 3 or greater")
    void cantAttackIntoUntappedBigCreature() {
        addCreatureReady(player2, new BalduvianBarbarians()); // 3/2
        mutant();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can't attack when the defending player controls an untapped creature with power greater than 3")
    void cantAttackIntoUntappedCreatureWithPowerGreaterThanThree() {
        addCreatureReady(player2, new JohtullWurm()); // 6/6
        mutant();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block an attacker with power below 3")
    void canBlockSmallAttacker() {
        Permanent mutant = mutant(player2);
        addCreatureReady(player1, new BalduvianBears()); // 2/2

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(mutant.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can't block an attacker with power 3 or greater")
    void cantBlockBigAttacker() {
        mutant(player2);
        addCreatureReady(player1, new BalduvianBarbarians()); // 3/2

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can't block an attacker with power greater than 3")
    void cantBlockAttackerWithPowerGreaterThanThree() {
        mutant(player2);
        addCreatureReady(player1, new JohtullWurm()); // 6/6

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        mutant();
        Permanent blocker = addCreatureReady(player2, new BalduvianBears()); // 2/2

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Own untapped large creatures do not prevent attacking")
    void ownLargeCreatureDoesNotPreventAttack() {
        Permanent mutant = mutant();
        addCreatureReady(player1, new BalduvianBarbarians());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(mutant.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Counters raising a defending creature's power to 3 prevent attacking")
    void cantAttackIntoCreatureRaisedToThreePower() {
        Permanent bear = addCreatureReady(player2, new BalduvianBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        mutant();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters lowering a defending creature's power below 3 permit attacking")
    void canAttackIntoCreatureReducedBelowThreePower() {
        Permanent barbarian = addCreatureReady(player2, new BalduvianBarbarians());
        barbarian.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent mutant = mutant();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(mutant.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Counters raising an attacker's power to 3 prevent blocking")
    void cantBlockCreatureRaisedToThreePower() {
        mutant(player2);
        Permanent bear = addCreatureReady(player1, new BalduvianBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters lowering an attacker's power below 3 permit blocking")
    void canBlockCreatureReducedBelowThreePower() {
        Permanent mutant = mutant(player2);
        Permanent barbarian = addCreatureReady(player1, new BalduvianBarbarians());
        barbarian.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(mutant.isBlocking()).isTrue();
    }
}
