package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasterfulReplicationTest extends BaseCardTest {

    @Test
    @DisplayName("The token mode creates two Golem tokens")
    void tokenModeCreatesTwoGolems() {
        castMasterfulReplication(0, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("The copy mode gives each other artifact you control the target artifact's ability")
    void copyModeAffectsOtherControlledArtifacts() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ZuranOrb());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new Forest());

        castMasterfulReplication(1, List.of(target.getId()));
        prepareMainPhase(player1);
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(otherArtifact.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("The temporary copies end at cleanup")
    void copyModeEndsAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ZuranOrb());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new MyrTurbine());

        castMasterfulReplication(1, List.of(target.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new Forest());
        prepareMainPhase(player1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        assertThat(otherArtifact.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("The copy mode only accepts an artifact you control as its target")
    void copyModeRejectsNonArtifactTarget() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        giveMasterfulReplication();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact you control");
    }

    private void castMasterfulReplication(int modeIndex, List<UUID> targetIds) {
        giveMasterfulReplication();
        if (targetIds.isEmpty()) {
            harness.castModalInstant(player1, 0, modeIndex, targetIds);
        } else {
            harness.castInstant(player1, 0, modeIndex, targetIds.getFirst());
        }
        harness.passBothPriorities();
    }

    private void giveMasterfulReplication() {
        harness.setHand(player1, List.of(new MasterfulReplication()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
