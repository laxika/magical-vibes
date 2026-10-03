package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoorToNothingness.class})
class DoorToNothingnessTest extends BaseCardTest {

    @Test
    @DisplayName("Door to Nothingness enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new DoorToNothingness(), "{5}");
        harness.passBothPriorities();

        Permanent door = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(door.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability makes the targeted opponent lose the game")
    void targetOpponentLosesTheGame() {
        harness.addToBattlefield(player1, new DoorToNothingness());
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The controller can target themselves and lose the game")
    void controllerCanTargetThemselves() {
        harness.addToBattlefield(player1, new DoorToNothingness());
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Activating the ability sacrifices Door to Nothingness as a cost")
    void sacrificesItselfAsCost() {
        harness.addToBattlefield(player1, new DoorToNothingness());
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Door to Nothingness");
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Colorless mana cannot replace either required mana of any color")
    void requiresTwoManaOfEveryColor(ManaColor missingColor) {
        Permanent door = harness.addToBattlefieldAndReturn(player1, new DoorToNothingness());
        for (ManaColor color : new ManaColor[]{ManaColor.WHITE, ManaColor.BLUE,
                ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN}) {
            harness.addMana(player1, color, color == missingColor ? 1 : 2);
        }
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(door.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Door to Nothingness");
        harness.assertNotInGraveyard(player1, "Door to Nothingness");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Cannot activate the ability while Door to Nothingness is tapped")
    void cannotActivateWhileTapped() {
        Permanent door = harness.addToBattlefieldAndReturn(player1, new DoorToNothingness());
        door.tap();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
