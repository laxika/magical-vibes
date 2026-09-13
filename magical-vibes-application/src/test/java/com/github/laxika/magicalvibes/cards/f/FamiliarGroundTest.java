package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FamiliarGround.class, GrizzlyBears.class})
class FamiliarGroundTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control can't be blocked by two creatures while Familiar Ground is out")
    void creatureCannotBeBlockedByTwoCreatures() {
        harness.addToBattlefield(player1, new FamiliarGround());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());

        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("A single blocker is legal while Familiar Ground is out")
    void canBeBlockedByOneCreature() {
        harness.addToBattlefield(player1, new FamiliarGround());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.getBlockingTargetIds()).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("Familiar Ground restricts every creature its controller controls")
    void restrictsEveryControlledCreature() {
        harness.addToBattlefield(player1, new FamiliarGround());

        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        secondAttacker.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 2),
                new BlockerAssignment(1, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Familiar Ground does not restrict creatures controlled by an opponent")
    void doesNotRestrictOpponentControlledCreature() {
        harness.addToBattlefield(player1, new FamiliarGround());
        Permanent blockerOne = addCreatureReady(player1, new GrizzlyBears());
        Permanent blockerTwo = addCreatureReady(player1, new GrizzlyBears());

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));

        assertThat(blockerOne.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(blockerTwo.getBlockingTargetIds()).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("Familiar Ground's restriction ends when it leaves the battlefield")
    void restrictionEndsWhenFamiliarGroundLeavesBattlefield() {
        Permanent familiarGround = harness.addToBattlefieldAndReturn(player1, new FamiliarGround());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent blockerOne = addCreatureReady(player2, new GrizzlyBears());
        Permanent blockerTwo = addCreatureReady(player2, new GrizzlyBears());

        gd.playerBattlefields.get(player1.getId()).remove(familiarGround);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(blockerOne.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(blockerTwo.getBlockingTargetIds()).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("Losing all abilities removes Familiar Ground's restriction")
    void losingAllAbilitiesRemovesRestriction() {
        Permanent familiarGround = harness.addToBattlefieldAndReturn(player1, new FamiliarGround());
        familiarGround.setLosesAllAbilitiesUntilEndOfTurn(true);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blockerOne = addCreatureReady(player2, new GrizzlyBears());
        Permanent blockerTwo = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)
        ));

        assertThat(blockerOne.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(blockerTwo.getBlockingTargetIds()).containsExactly(attacker.getId());
    }
}
