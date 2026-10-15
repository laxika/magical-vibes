package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CaptivatingCrossroads.class)
class CaptivatingCrossroadsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped during the starting player's first three turns")
    void entersTappedDuringStartingPlayersFirstThreeTurns() {
        gd.turnsTakenByPlayer.put(player1.getId(), 3);
        playCrossroads(player1);

        Permanent crossroads = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(crossroads.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped for a player who was not the starting player")
    void entersUntappedForNonStartingPlayer() {
        harness.forceActivePlayer(player2);
        gd.startingPlayerId = player1.getId();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.turnsTakenByPlayer.put(player2.getId(), 3);
        playCrossroads(player2);

        Permanent crossroads = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(crossroads.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped after the starting player's third turn")
    void entersUntappedAfterStartingPlayersThirdTurn() {
        gd.turnsTakenByPlayer.put(player1.getId(), 4);
        playCrossroads(player1);

        Permanent crossroads = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(crossroads.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Chooses a color and taps for one mana of that color")
    void tapsForChosenColor() {
        gd.turnsTakenByPlayer.put(player1.getId(), 4);
        playCrossroads(player1);
        Permanent crossroads = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(crossroads.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
    }

    private void playCrossroads(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new CaptivatingCrossroads()));
        harness.playLand(player, 0);
        harness.handleListChoice(player, ManaColor.GREEN.name());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void entersTappedDuringStartingPlayersEarlierTurns(int turn) {
        gd.turnsTakenByPlayer.put(player1.getId(), turn);
        playCrossroads(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void producesOnlyTheChosenColorImmediately(ManaColor color) {
        gd.turnsTakenByPlayer.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new CaptivatingCrossroads()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, color.name());
        harness.activateAbility(player1, 0, 0, null, null);

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor == color ? 1 : 0);
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void entersUntappedDuringOpponentsTurnEvenBeforeStartingPlayersFourthTurn() {
        gd.activePlayerId = player2.getId();
        gd.turnsTakenByPlayer.put(player1.getId(), 1);
        Permanent crossroads = harness.enterBattlefieldAndReturn(player1, new CaptivatingCrossroads());

        assertThat(crossroads.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
