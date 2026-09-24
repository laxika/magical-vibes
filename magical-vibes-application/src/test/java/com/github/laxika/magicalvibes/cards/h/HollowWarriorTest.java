package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HollowWarrior.class, PygmyRazorback.class})
class HollowWarriorTest extends BaseCardTest {

    @Test
    void tapsAnotherCreatureToAttack() {
        Permanent warrior = addCreatureReady(player1, new HollowWarrior());
        Permanent support = addCreatureReady(player1, new PygmyRazorback());

        declareAttackers(player1, List.of(0));

        assertThat(warrior.isTapped()).isTrue();
        assertThat(support.isTapped()).isTrue();
    }

    @Test
    void cannotUseAnotherDeclaredAttackerToPay() {
        addCreatureReady(player1, new HollowWarrior());
        addCreatureReady(player1, new PygmyRazorback());

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
    void cannotAttackWithOnlyTappedCreatureToPay() {
        addCreatureReady(player1, new HollowWarrior());
        Permanent support = addCreatureReady(player1, new PygmyRazorback());
        support.tap();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void tapsAnotherCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new PygmyRazorback());
        attacker.setAttacking(true);
        Permanent warrior = addCreatureReady(player2, new HollowWarrior());
        Permanent support = addCreatureReady(player2, new PygmyRazorback());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(warrior.isBlocking()).isTrue();
        assertThat(support.isTapped()).isTrue();
    }

    @Test
    void cannotBlockWithoutAnotherUntappedCreature() {
        Permanent attacker = addCreatureReady(player1, new PygmyRazorback());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HollowWarrior());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void cannotUseAnotherDeclaredBlockerToPay() {
        Permanent attacker = addCreatureReady(player1, new PygmyRazorback());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HollowWarrior());
        addCreatureReady(player2, new PygmyRazorback());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped creatures to block");
    }

    @Test
    void cannotBlockWithOnlyTappedCreatureToPay() {
        Permanent attacker = addCreatureReady(player1, new PygmyRazorback());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HollowWarrior());
        Permanent support = addCreatureReady(player2, new PygmyRazorback());
        support.tap();

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @CardUsed(Humility.class)
    void losingAllAbilitiesRemovesTheAttackTapCost() {
        harness.addToBattlefieldAndReturn(player1, new Humility());
        Permanent warrior = addCreatureReady(player1, new HollowWarrior());

        declareAttackers(player1, List.of(1));

        assertThat(warrior.isTapped()).isTrue();
    }

    @Test
    @CardUsed(Humility.class)
    void losingAllAbilitiesRemovesTheBlockTapCost() {
        Permanent attacker = addCreatureReady(player1, new PygmyRazorback());
        attacker.setAttacking(true);
        harness.addToBattlefieldAndReturn(player2, new Humility());
        Permanent warrior = addCreatureReady(player2, new HollowWarrior());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(warrior.isBlocking()).isTrue();
    }
}
