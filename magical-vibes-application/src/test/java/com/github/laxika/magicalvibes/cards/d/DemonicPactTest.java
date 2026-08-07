package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemonicPactTest extends BaseCardTest {

    private static final String DAMAGE = "Demonic Pact deals 4 damage to any target and you gain 4 life";
    private static final String DISCARD = "Target opponent discards two cards";
    private static final String DRAW = "Draw two cards";
    private static final String LOSE = "You lose the game";

    @Test
    @DisplayName("Damage mode deals 4 to a chosen player and gains 4 life")
    void damageModeHitsPlayer() {
        harness.addToBattlefield(player1, new DemonicPact());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Damage mode can kill a creature")
    void damageModeHitsCreature() {
        harness.addToBattlefield(player1, new DemonicPact());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, bears.getId())).isNull();
    }

    @Test
    @DisplayName("Discard mode makes the targeted opponent discard two cards")
    void discardMode() {
        harness.addToBattlefield(player1, new DemonicPact());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, DISCARD);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Discard mode cannot target the controller")
    void discardModeCannotTargetSelf() {
        harness.addToBattlefield(player1, new DemonicPact());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, DISCARD);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draw mode draws two cards")
    void drawMode() {
        harness.addToBattlefield(player1, new DemonicPact());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, DRAW);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Lose-the-game mode ends the game with the opponent winning")
    void loseMode() {
        harness.addToBattlefield(player1, new DemonicPact());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, LOSE);
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A mode already chosen is not offered again")
    void chosenModeIsConsumed() {
        harness.addToBattlefield(player1, new DemonicPact());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, DRAW);
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.handleListChoice(player1, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new DemonicPact());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
