package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SerraAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Not castable during the controller's first turn")
    void notCastableOnFirstTurn() {
        harness.setHand(player1, List.of(new SerraAvenger()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Not castable during the controller's third turn")
    void notCastableOnThirdTurn() {
        gd.turnsTakenByPlayer.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new SerraAvenger()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Castable during the controller's fourth turn")
    void castableOnFourthTurn() {
        gd.turnsTakenByPlayer.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new SerraAvenger()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Avenger");
    }

    @Test
    @DisplayName("Only the caster's own turns count — an opponent's fourth turn does not unlock it")
    void opponentTurnCountDoesNotUnlock() {
        gd.turnsTakenByPlayer.put(player2.getId(), 4);
        harness.setHand(player1, List.of(new SerraAvenger()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
