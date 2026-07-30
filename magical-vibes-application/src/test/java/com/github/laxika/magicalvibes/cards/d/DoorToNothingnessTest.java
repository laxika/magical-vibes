package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DoorToNothingnessTest extends BaseCardTest {

    @Test
    @DisplayName("Door to Nothingness enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new DoorToNothingness()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
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
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
