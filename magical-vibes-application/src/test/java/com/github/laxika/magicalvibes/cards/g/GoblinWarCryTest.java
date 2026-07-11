package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinWarCryTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen creature can still block; the target opponent's other creatures can't")
    void chosenCreatureCanBlockOthersCant() {
        Permanent kept = addReadyCreature(player2);
        Permanent other = addReadyCreature(player2);

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        assertThat(kept.isCantBlockThisTurn()).isFalse();
        assertThat(other.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Every creature except the chosen one can't block")
    void allButChosenCantBlock() {
        Permanent kept = addReadyCreature(player2);
        Permanent other1 = addReadyCreature(player2);
        Permanent other2 = addReadyCreature(player2);

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        assertThat(kept.isCantBlockThisTurn()).isFalse();
        assertThat(other1.isCantBlockThisTurn()).isTrue();
        assertThat(other2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A lone creature is never restricted (no other creatures)")
    void singleCreatureNotRestricted() {
        Permanent only = addReadyCreature(player2);

        castGoblinWarCry();

        assertThat(only.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Resolves harmlessly when the target opponent controls no creatures")
    void noCreaturesResolvesHarmlessly() {
        castGoblinWarCry();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The controller's own creatures are unaffected")
    void controllersCreaturesUnaffected() {
        Permanent own = addReadyCreature(player1);
        Permanent kept = addReadyCreature(player2);
        Permanent other = addReadyCreature(player2);

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        assertThat(own.isCantBlockThisTurn()).isFalse();
        assertThat(other.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A restricted creature can't be declared as a blocker")
    void restrictedCreatureCantBlock() {
        Permanent attacker = addReadyCreature(player1);
        Permanent kept = addReadyCreature(player2);
        addReadyCreature(player2); // the restricted blocker (index 1)

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can't target the caster's own player")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new GoblinWarCry()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGoblinWarCry() {
        harness.setHand(player1, List.of(new GoblinWarCry()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        perm.setSummoningSick(false);
        return perm;
    }
}
