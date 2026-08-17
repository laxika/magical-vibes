package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PistonFistCyclopsTest extends BaseCardTest {

    @BeforeEach
    void setUpTest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Can attack after its controller casts an instant")
    void canAttackAfterInstant() {
        addCreatureReady(player1, new PistonFistCyclops());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        declareAttackers(List.of(0));

        Permanent cyclops = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cyclops.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can attack after its controller casts a sorcery")
    void canAttackAfterSorcery() {
        addCreatureReady(player1, new PistonFistCyclops());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        declareAttackers(List.of(0));

        Permanent cyclops = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cyclops.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot attack when only a creature spell was cast")
    void cannotAttackAfterCreatureSpell() {
        addCreatureReady(player1, new PistonFistCyclops());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack permission wears off at the end of the turn")
    void attackPermissionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new PistonFistCyclops());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
