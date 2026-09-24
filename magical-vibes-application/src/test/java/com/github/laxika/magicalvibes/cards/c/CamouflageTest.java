package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Camouflage.class, GrizzlyBears.class})
class CamouflageTest extends BaseCardTest {

    @Test
    @DisplayName("assigns the only pile to the only attacker")
    void assignsPileToOnlyAttacker() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        castCamouflage();
        harness.handleMultiplePermanentsChosen(player2, List.of(blocker.getId()));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).hasSize(1);
    }

    @Test
    @DisplayName("an empty pile leaves the attacker unblocked")
    void emptyPileLeavesAttackerUnblocked() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        castCamouflage();
        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(attacker.getBlockingTargetIds()).isEmpty();
    }

    @Test
    @DisplayName("can only be cast during declare attackers")
    void hasDeclareAttackersTimingRestriction() {
        harness.setHand(player1, List.of(new Camouflage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void castCamouflage() {
        harness.setHand(player1, List.of(new Camouflage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
