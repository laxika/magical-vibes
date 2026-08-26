package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelesnyaSagittars.class, GrizzlyBears.class})
class SelesnyaSagittarsTest extends BaseCardTest {

    @Test
    @DisplayName("Selesnya Sagittars can block two attackers")
    void canBlockTwoAttackers() {
        Permanent sagittars = addSagittars();
        int sagittarsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(sagittars);
        addAttackers(2);
        enterBlockerDeclaration();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(sagittarsIndex, 0),
                new BlockerAssignment(sagittarsIndex, 1)
        ));

        assertThat(sagittars.isBlocking()).isTrue();
        assertThat(sagittars.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Selesnya Sagittars cannot block three attackers")
    void cannotBlockThreeAttackers() {
        Permanent sagittars = addSagittars();
        int sagittarsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(sagittars);
        addAttackers(3);
        enterBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(sagittarsIndex, 0),
                new BlockerAssignment(sagittarsIndex, 1),
                new BlockerAssignment(sagittarsIndex, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Selesnya Sagittars does not grant additional blocks to other creatures")
    void doesNotGrantAdditionalBlocksToOthers() {
        addSagittars();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        int bearsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);

        addAttackers(2);
        enterBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(bearsIndex, 0),
                new BlockerAssignment(bearsIndex, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    private Permanent addSagittars() {
        Permanent perm = new Permanent(new SelesnyaSagittars());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(perm);
        return perm;
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent attacker = new Permanent(new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(attacker);
        }
    }

    private void enterBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
