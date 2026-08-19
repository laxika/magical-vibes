package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HollowWarriorTest extends BaseCardTest {

    @Test
    void tapsAnotherCreatureToAttack() {
        Permanent warrior = addCreatureReady(player1, new HollowWarrior());
        Permanent support = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(warrior.isTapped()).isTrue();
        assertThat(support.isTapped()).isTrue();
    }

    @Test
    void cannotUseAnotherDeclaredAttackerToPay() {
        addCreatureReady(player1, new HollowWarrior());
        addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped creatures to attack");
    }

    @Test
    void cannotAttackWithoutAnotherUntappedCreature() {
        addCreatureReady(player1, new HollowWarrior());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void tapsAnotherCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent warrior = addCreatureReady(player2, new HollowWarrior());
        Permanent support = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(warrior.isBlocking()).isTrue();
        assertThat(support.isTapped()).isTrue();
    }

    @Test
    void cannotBlockWithoutAnotherUntappedCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HollowWarrior());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
