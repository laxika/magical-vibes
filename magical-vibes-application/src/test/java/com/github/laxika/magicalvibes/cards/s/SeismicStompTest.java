package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeismicStompTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures without flying can't block this turn")
    void nonFliersCantBlock() {
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        castSeismicStomp();

        assertThat(bears.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Creatures with flying are unaffected")
    void fliersUnaffected() {
        Permanent drake = addReadyCreature(player2, new WindDrake());

        castSeismicStomp();

        assertThat(drake.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Applies to both players' non-flying creatures")
    void affectsAllPlayers() {
        Permanent ownBears = addReadyCreature(player1, new GrizzlyBears());
        Permanent oppBears = addReadyCreature(player2, new GrizzlyBears());
        Permanent oppDrake = addReadyCreature(player2, new WindDrake());

        castSeismicStomp();

        assertThat(ownBears.isCantBlockThisTurn()).isTrue();
        assertThat(oppBears.isCantBlockThisTurn()).isTrue();
        assertThat(oppDrake.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("A restricted non-flier can't be declared as a blocker")
    void restrictedCreatureCantBeDeclaredBlocker() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());

        castSeismicStomp();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A flier can still be declared as a blocker")
    void flierCanStillBlock() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent drake = addReadyCreature(player2, new WindDrake());

        castSeismicStomp();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(drake.isBlocking()).isTrue();
    }

    private void castSeismicStomp() {
        harness.setHand(player1, List.of(new SeismicStomp()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }
}
