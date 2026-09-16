package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronfistCrusher.class, GlorySeeker.class})
class IronfistCrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Ironfist Crusher can block any number of creatures")
    void canBlockAnyNumberOfCreatures() {
        Permanent crusher = addCreatureReady(player2, new IronfistCrusher());
        addReadyAttacker(player1);
        addReadyAttacker(player1);
        addReadyAttacker(player1);

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)));

        assertThat(crusher.isBlocking()).isTrue();
        assertThat(crusher.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("Ironfist Crusher can be cast face down and turned face up for morph")
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new IronfistCrusher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent crusher = findPermanent(player1, "Ironfist Crusher");
        assertThat(crusher.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int crusherIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crusher);
        harness.turnFaceUp(player1, crusherIndex);
        harness.passBothPriorities();

        assertThat(crusher.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Face-down Ironfist Crusher cannot block more than one creature")
    void faceDownDoesNotHaveItsBlockAnyNumberAbility() {
        harness.setHand(player2, List.of(new IronfistCrusher()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent crusher = findPermanent(player2, "Ironfist Crusher");
        assertThat(crusher.isFaceDown()).isTrue();

        addReadyAttacker(player1);
        addReadyAttacker(player1);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("assigned too many times");
    }

    private void addReadyAttacker(Player player) {
        Permanent permanent = addCreatureReady(player, new GlorySeeker());
        permanent.setAttacking(true);
    }
}
