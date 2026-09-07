package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HungeringYetiTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast as though it had flash while controlling a green permanent")
    void canBeCastWithGreenPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareCastOnOpponentTurn();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can be cast as though it had flash while controlling a blue permanent")
    void canBeCastWithBluePermanent() {
        harness.addToBattlefield(player1, new CloudSprite());
        prepareCastOnOpponentTurn();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot be cast at instant timing without controlling a green or blue permanent")
    void cannotBeCastWithoutMatchingPermanent() {
        prepareCastOnOpponentTurn();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's green permanent does not grant flash")
    void opponentPermanentDoesNotGrantFlash() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareCastOnOpponentTurn();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCastOnOpponentTurn() {
        harness.setHand(player1, List.of(new HungeringYeti()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
