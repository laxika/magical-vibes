package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FalterTest extends BaseCardTest {

    @Test
    @DisplayName("A creature without flying can't block this turn")
    void creatureWithoutFlyingCannotBlock() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());

        castFalter();
        prepareDeclareBlockers(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A creature with flying can still block this turn")
    void creatureWithFlyingCanBlock() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent blocker = addReadyCreature(player2, new AirElemental());

        castFalter();
        prepareDeclareBlockers(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("It affects creatures controlled by either player")
    void affectsBothPlayers() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent ownGroundCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentGroundCreature = addReadyCreature(player2, new GrizzlyBears());

        castFalter();

        assertThat(ownGroundCreature.isCantBlockThisTurn()).isTrue();
        assertThat(opponentGroundCreature.isCantBlockThisTurn()).isTrue();
        assertThat(attacker.isCantBlockThisTurn()).isTrue();
    }

    private void castFalter() {
        harness.setHand(player1, List.of(new Falter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void prepareDeclareBlockers(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
