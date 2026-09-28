package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReturnThePast.class, Shock.class, GrizzlyBears.class})
class ReturnThePastTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, instant and sorcery cards in your graveyard have flashback")
    void grantsFlashbackDuringYourTurn() {
        harness.addToBattlefield(player1, new ReturnThePast());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock()));
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Return the Past does not grant flashback during an opponent's turn")
    void grantsFlashbackOnlyDuringYourTurn() {
        harness.addToBattlefield(player1, new ReturnThePast());
        harness.setGraveyard(player1, List.of(new Shock()));
        prepareMainPhase(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Return the Past does not grant flashback to creature cards")
    void doesNotGrantFlashbackToCreatures() {
        harness.addToBattlefield(player1, new ReturnThePast());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
