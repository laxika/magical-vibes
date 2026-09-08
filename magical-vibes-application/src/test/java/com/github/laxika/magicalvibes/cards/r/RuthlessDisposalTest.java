package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WorldspineWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuthlessDisposalTest extends BaseCardTest {

    @Test
    @DisplayName("Gives two target creatures -13/-13 until end of turn")
    void givesBothTargetsMinusThirteenMinusThirteen() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRuthlessDisposal(List.of(first.getId(), second.getId()), sacrifice.getId());

        assertThat(first.getPowerModifier()).isEqualTo(-13);
        assertThat(first.getToughnessModifier()).isEqualTo(-13);
        assertThat(second.getPowerModifier()).isEqualTo(-13);
        assertThat(second.getToughnessModifier()).isEqualTo(-13);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("The same creature can be chosen twice")
    void sameTargetGetsBothDebuffs() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRuthlessDisposal(List.of(target.getId(), target.getId()), sacrifice.getId());

        harness.assertNotOnBattlefield(player2, "Worldspine Wurm");
        harness.assertInGraveyard(player2, "Worldspine Wurm");
    }

    @Test
    @DisplayName("The debuffs wear off at end of turn")
    void debuffsWearOffAtEndOfTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRuthlessDisposal(List.of(first.getId(), second.getId()), sacrifice.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(0);
        assertThat(first.getToughnessModifier()).isEqualTo(0);
        assertThat(second.getPowerModifier()).isEqualTo(0);
        assertThat(second.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        harness.setHand(player1, List.of(new RuthlessDisposal(), new AirElemental()));
        addMana();

        assertThatThrownBy(() -> playRuthlessDisposal(
                List.of(target.getId(), secondTarget.getId()), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    private void castRuthlessDisposal(List<java.util.UUID> targetIds, java.util.UUID sacrificeId) {
        harness.setHand(player1, List.of(new RuthlessDisposal(), new AirElemental()));
        addMana();
        playRuthlessDisposal(targetIds, sacrificeId);
        harness.passBothPriorities();
    }

    private void playRuthlessDisposal(List<java.util.UUID> targetIds, java.util.UUID sacrificeId) {
        gs.playCard(gd, player1, 0, 0, null, null, targetIds, List.of(), false, sacrificeId,
                null, List.of(), null, List.of(), false, 1, null, null, null);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
