package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazeTheEffigy.class, GrizzlyBears.class, Ornithopter.class})
class RazeTheEffigyTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new RazeTheEffigy()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    void cannotDestroyNonArtifactWithArtifactMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RazeTheEffigy()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostsTargetAttackingCreatureUntilEndOfTurn() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RazeTheEffigy()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, 1, List.of(attacker.getId()));
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(4);
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void cannotTargetNonAttackingCreatureWithBoostMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RazeTheEffigy()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
