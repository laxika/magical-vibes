package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Exploration.class, Forest.class})
class ExplorationTest extends BaseCardTest {

    @Test
    @DisplayName("Exploration grants its controller one additional land play")
    void grantsControllerOneAdditionalLandPlay() {
        harness.addToBattlefield(player1, new Exploration());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller can play two lands in one turn")
    void controllerCanPlayTwoLandsInOneTurn() {
        harness.addToBattlefield(player1, new Exploration());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The controller cannot play more than two lands in one turn")
    void controllerCannotPlayMoreThanTwoLandsInOneTurn() {
        harness.addToBattlefield(player1, new Exploration());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Explorations each grant an additional land play")
    void multipleCopiesGrantThreeLandPlays() {
        harness.addToBattlefield(player1, new Exploration());
        harness.addToBattlefield(player1, new Exploration());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(5);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Exploration does not grant extra land plays")
    void opponentsExplorationDoesNotGrantExtraLandPlays() {
        harness.addToBattlefield(player2, new Exploration());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exploration does not allow land plays outside a main phase")
    void doesNotChangeLandPlayTiming() {
        harness.addToBattlefield(player1, new Exploration());
        harness.setHand(player1, List.of(new Forest()));
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exploration does not allow land plays on an opponent's turn")
    void doesNotAllowLandPlaysOnOpponentsTurn() {
        harness.addToBattlefield(player2, new Exploration());
        harness.setHand(player2, List.of(new Forest()));

        assertThatThrownBy(() -> harness.playLand(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving Exploration after the first land allows a second land")
    void resolvingAfterFirstLandGrantsAnotherPlay() {
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.castFromHand(player1, new Exploration(), "{G}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
