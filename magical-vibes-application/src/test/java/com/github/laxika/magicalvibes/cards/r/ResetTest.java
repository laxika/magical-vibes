package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reset.class, Island.class, GrizzlyBears.class})
class ResetTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all lands controlled by its caster")
    void untapsControlledLandsOnly() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.tap();
        Permanent opponentIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        opponentIsland.tap();

        prepareCast();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(island.isTapped()).isFalse();
        assertThat(bear.isTapped()).isTrue();
        assertThat(opponentIsland.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can be cast during an opponent's draw step after upkeep")
    void canBeCastAfterOpponentsUpkeep() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        prepareCast();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(island.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot be cast during an opponent's upkeep")
    void cannotBeCastDuringOpponentsUpkeep() {
        prepareCast();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot be cast during its controller's turn")
    void cannotBeCastDuringControllersTurn() {
        prepareCast();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new Reset()));
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
