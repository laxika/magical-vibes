package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(StartingTown.class)
class StartingTownTest extends BaseCardTest {

    @Test
    @DisplayName("Enters untapped during the controller's first three turns")
    void entersUntappedDuringFirstThreeTurns() {
        gd.turnsTakenByPlayer.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new StartingTown()));

        harness.playLand(player1, 0);

        Permanent town = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(town.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters tapped after the controller's third turn")
    void entersTappedAfterThirdTurn() {
        gd.turnsTakenByPlayer.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new StartingTown()));

        harness.playLand(player1, 0);

        Permanent town = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(town.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps for one colorless mana")
    void tapsForColorlessMana() {
        Permanent town = harness.addToBattlefieldAndReturn(player1, new StartingTown());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(town.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays 1 life and adds one mana of the chosen color")
    void paysLifeAndAddsChosenColor() {
        Permanent town = harness.addToBattlefieldAndReturn(player1, new StartingTown());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(town.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Enters untapped on the controller's first and second turns")
    void entersUntappedOnEarlierTurns(int turnCount) {
        gd.turnsTakenByPlayer.put(player1.getId(), turnCount);
        harness.setHand(player1, List.of(new StartingTown()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    @DisplayName("Enters tapped on an opponent's turn even early in the game")
    void entersTappedOnOpponentsTurn(int turnCount) {
        gd.turnsTakenByPlayer.put(player1.getId(), turnCount);
        harness.forceActivePlayer(player2);

        Permanent town = harness.enterBattlefieldAndReturn(player1, new StartingTown());

        assertThat(town.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entry uses the controller's turn count rather than the opponent's")
    void ignoresOpponentsTurnCount() {
        gd.turnsTakenByPlayer.put(player1.getId(), 2);
        gd.turnsTakenByPlayer.put(player2.getId(), 7);
        harness.setHand(player1, List.of(new StartingTown()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The life-payment mana ability can produce each color without using the stack")
    void producesEachColorImmediately(ManaColor color) {
        Permanent town = harness.addToBattlefieldAndReturn(player1, new StartingTown());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(town.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        assertThat(gd.stack).isEmpty();
        for (ManaColor candidate : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(candidate))
                    .isEqualTo(candidate == color ? 1 : 0);
        }
    }

    @Test
    @DisplayName("Cannot activate the colored mana ability without enough life")
    void cannotPayLifeAtZeroLife() {
        Permanent town = harness.addToBattlefieldAndReturn(player1, new StartingTown());
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(town.isTapped()).isFalse();
        harness.assertLife(player1, 0);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither mana ability can be activated while the land is tapped")
    void cannotActivateTappedLand(int abilityIndex) {
        Permanent town = harness.addToBattlefieldAndReturn(player1, new StartingTown());
        town.tap();
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
