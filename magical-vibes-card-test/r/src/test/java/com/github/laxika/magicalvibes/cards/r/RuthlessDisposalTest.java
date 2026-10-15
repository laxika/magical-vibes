package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WorldspineWurm;
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

@CardUsed({RuthlessDisposal.class, AirElemental.class, GrizzlyBears.class, WorldspineWurm.class})
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
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new RuthlessDisposal(), new AirElemental()));
        addMana();
        assertThatThrownBy(() -> playRuthlessDisposal(
                List.of(target.getId(), target.getId()), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The debuffs wear off at end of turn")
    void debuffsWearOffAtEndOfTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRuthlessDisposal(List.of(first.getId(), second.getId()), sacrifice.getId());

        harness.forceStep(TurnStep.END_STEP);
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

    @Test
    @DisplayName("Requires two targets even when another creature can be sacrificed")
    void cannotCastWithOnlyOneTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RuthlessDisposal(), new AirElemental()));
        addMana();

        assertThatThrownBy(() -> playRuthlessDisposal(List.of(target.getId()), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use the spell itself as the discarded card")
    void cannotCastWithoutAnotherCardToDiscard() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RuthlessDisposal()));
        addMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(first.getId(), second.getId()), List.of(), false, sacrifice.getId(),
                null, List.of(), null, List.of(), false, 0, null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard");
    }

    @Test
    @DisplayName("Discard and sacrifice are paid before the spell resolves")
    void paysAdditionalCostsBeforeResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RuthlessDisposal(), new AirElemental()));
        addMana();

        playRuthlessDisposal(List.of(first.getId(), second.getId()), sacrifice.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Air Elemental");
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A targeted creature may be sacrificed to pay the cost and the other target still resolves")
    void sacrificedTargetDoesNotPreventOtherTargetResolving() {
        Permanent survivingTarget = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        Permanent sacrificedTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRuthlessDisposal(List.of(sacrificedTarget.getId(), survivingTarget.getId()),
                sacrificedTarget.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(survivingTarget.getPowerModifier()).isEqualTo(-13);
        assertThat(survivingTarget.getToughnessModifier()).isEqualTo(-13);
    }

    @Test
    @DisplayName("Both creatures die when their toughness is reduced to zero or less")
    void killsBothTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRuthlessDisposal(List.of(first.getId(), second.getId()), sacrifice.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Air Elemental");
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
