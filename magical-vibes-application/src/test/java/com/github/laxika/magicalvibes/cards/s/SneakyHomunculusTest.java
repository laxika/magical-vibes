package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SneakyHomunculusTest extends BaseCardTest {

    @Test
    @DisplayName("Sneaky Homunculus can block a creature with power 1")
    void canBlockLowPowerCreature() {
        Permanent homunculus = new Permanent(new SneakyHomunculus());
        homunculus.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(homunculus);

        Permanent atkPerm = new Permanent(new FugitiveWizard()); // 1/1
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(homunculus.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sneaky Homunculus cannot block a creature with power 2 or greater")
    void cannotBlockHighPowerCreature() {
        Permanent homunculus = new Permanent(new SneakyHomunculus());
        homunculus.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(homunculus);

        Permanent atkPerm = new Permanent(new GrizzlyBears()); // 2/2
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with power 1 or less");
    }

    @Test
    @DisplayName("Sneaky Homunculus can be blocked by a creature with power 1")
    void canBeBlockedByLowPowerCreature() {
        Permanent homunculus = new Permanent(new SneakyHomunculus());
        homunculus.setSummoningSick(false);
        homunculus.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(homunculus);

        Permanent blockerPerm = new Permanent(new FugitiveWizard()); // 1/1
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sneaky Homunculus cannot be blocked by a creature with power 2 or greater")
    void cannotBeBlockedByHighPowerCreature() {
        Permanent homunculus = new Permanent(new SneakyHomunculus());
        homunculus.setSummoningSick(false);
        homunculus.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(homunculus);

        Permanent blockerPerm = new Permanent(new GrizzlyBears()); // 2/2
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }
}
