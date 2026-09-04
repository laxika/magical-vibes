package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GriffinCanyon;
import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForbiddenRitual.class, Python.class, GriffinCanyon.class})
class ForbiddenRitualTest extends BaseCardTest {

    private void castAt(UUID opponentId) {
        harness.setHand(player1, List.of(new ForbiddenRitual()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, opponentId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("With one nontoken permanent, auto-sacrifices it and opponent loses life when that is the only option")
    void singleNontokenAutoSacrificesAndOpponentLosesLife() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new Python());
        castAt(player2.getId());

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Python");
        harness.assertInGraveyard(player1, "Python");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("With no nontoken permanent the spell does nothing")
    void noNontokenDoesNothing() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Python()));
        castAt(player2.getId());

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Opponent may sacrifice any permanent including a land")
    void opponentMaySacrificeALand() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new Python());
        UUID canyonId = harness.addToBattlefieldAndReturn(player2, new GriffinCanyon()).getId();
        castAt(player2.getId());

        harness.handleListChoice(player2, ChoiceContext.ForbiddenRitualPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, canyonId);

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Griffin Canyon");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Opponent may discard a card instead of losing life")
    void opponentMayDiscard() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Python()));
        harness.addToBattlefield(player1, new Python());
        castAt(player2.getId());

        harness.handleListChoice(player2, ChoiceContext.ForbiddenRitualPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Python");
    }

    @Test
    @DisplayName("Opponent may choose to lose life when sacrifice and discard alternatives exist")
    void opponentMayChooseLifeInstead() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Python()));
        harness.addToBattlefield(player1, new Python());
        harness.addToBattlefield(player2, new GriffinCanyon());
        castAt(player2.getId());

        harness.handleListChoice(player2, "Lose 2 life");

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Griffin Canyon");
        harness.assertInHand(player2, "Python");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Repeat can continue after the opponent discards")
    void repeatAfterOpponentDiscards() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Python()));
        UUID first = harness.addToBattlefieldAndReturn(player1, new Python()).getId();
        harness.addToBattlefield(player1, new Python());
        castAt(player2.getId());

        harness.handlePermanentChosen(player1, first);
        harness.handleListChoice(player2, ChoiceContext.ForbiddenRitualPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Python");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Controller may repeat: second cycle sacrifices another permanent")
    void mayRepeatProcess() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        UUID first = harness.addToBattlefieldAndReturn(player1, new Python()).getId();
        harness.addToBattlefield(player1, new Python());
        castAt(player2.getId());

        harness.handlePermanentChosen(player1, first);
        harness.assertLife(player2, 18);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 16);
        harness.assertNotOnBattlefield(player1, "Python");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining the repeat ends the process")
    void declineRepeatEnds() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        UUID first = harness.addToBattlefieldAndReturn(player1, new Python()).getId();
        harness.addToBattlefield(player1, new Python());
        castAt(player2.getId());

        harness.handlePermanentChosen(player1, first);
        harness.assertLife(player2, 18);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Python");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Token permanents do not satisfy the controller's nontoken sacrifice")
    void tokenPermanentsAreNotSacrificed() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        Python token = new Python();
        token.setToken(true);
        harness.addToBattlefield(player1, token);
        castAt(player2.getId());

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Python");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
