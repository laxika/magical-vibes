package com.github.laxika.magicalvibes.cards.b;

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

class BrassclawOrcsTest extends BaseCardTest {

    @Test
    @DisplayName("Brassclaw Orcs can block a creature with power 1 or less")
    void canBlockLowPowerCreature() {
        Permanent orcs = new Permanent(new BrassclawOrcs());
        orcs.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(orcs);

        Permanent atkPerm = new Permanent(new FugitiveWizard()); // 1/1
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(orcs.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Brassclaw Orcs cannot block a creature with power 2 or greater")
    void cannotBlockHighPowerCreature() {
        Permanent orcs = new Permanent(new BrassclawOrcs());
        orcs.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(orcs);

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
}
