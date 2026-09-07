package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MazeOfIth;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TangleKelp.class, MazeOfIth.class, Squire.class})
class TangleKelpTest extends BaseCardTest {

    @Test
    @DisplayName("Tangle Kelp taps the enchanted creature when it enters")
    void tapsEnchantedCreatureOnEnter() {
        Permanent creature = addCreatureReady(player2, new Squire());

        harness.setHand(player1, List.of(new TangleKelp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tangle Kelp prevents a creature that attacked last turn from untapping")
    void preventsUntapAfterCreatureAttackedLastTurn() {
        Permanent creature = addCreatureReady(player2, new Squire());
        creature.tap();
        creature.setAttackedDuringControllersCurrentTurn(true);
        attachTangleKelp(creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tangle Kelp does not prevent untapping when the creature did not attack last turn")
    void allowsUntapAfterCreatureDidNotAttackLastTurn() {
        Permanent creature = addCreatureReady(player2, new Squire());
        creature.tap();
        attachTangleKelp(creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tangle Kelp's restriction expires after a controller turn without attacking")
    void allowsUntapAfterAnIdleTurn() {
        Permanent creature = addCreatureReady(player2, new Squire());
        creature.tap();
        creature.setAttackedDuringControllersCurrentTurn(true);
        attachTangleKelp(creature);

        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isTrue();

        advanceToNextTurn(player2);
        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tangle Kelp cannot enchant a land")
    void cannotEnchantLand() {
        harness.addToBattlefield(player2, new MazeOfIth());
        Permanent land = findPermanent(player2, "Maze of Ith");

        harness.setHand(player1, List.of(new TangleKelp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void attachTangleKelp(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TangleKelp());
        aura.setAttachedTo(creature.getId());
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player nextActivePlayer = currentActivePlayer.getId().equals(player1.getId()) ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.UNTAP);
    }
}
