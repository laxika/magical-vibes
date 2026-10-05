package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PendulumOfPatterns.class})
class PendulumOfPatternsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield causes its controller to gain 3 life")
    void enteringBattlefieldGainsLife() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new PendulumOfPatterns(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Paying 5 mana and tapping it sacrifices it and draws a card")
    void activatedAbilitySacrificesAndDraws() {
        harness.addToBattlefield(player1, new PendulumOfPatterns());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertNotOnBattlefield(player1, "Pendulum of Patterns");
        harness.assertInGraveyard(player1, "Pendulum of Patterns");
    }

    @Test
    @DisplayName("Sacrifice is paid immediately but the card is drawn only on resolution")
    void sacrificePrecedesDraw() {
        harness.addToBattlefield(player1, new PendulumOfPatterns());
        harness.setLibrary(player1, List.of(new PendulumOfPatterns()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Pendulum of Patterns");
        harness.assertInGraveyard(player1, "Pendulum of Patterns");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player1, "Pendulum of Patterns");
    }

    @Test
    @DisplayName("A tapped Pendulum cannot activate its ability")
    void tappedArtifactCannotActivate() {
        harness.addToBattlefieldAndReturn(player1, new PendulumOfPatterns()).setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pendulum of Patterns");
        harness.assertNotInGraveyard(player1, "Pendulum of Patterns");
    }

    @Test
    @DisplayName("Four mana cannot pay the activation cost")
    void insufficientManaCannotActivate() {
        harness.addToBattlefield(player1, new PendulumOfPatterns());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pendulum of Patterns");
        harness.assertNotInGraveyard(player1, "Pendulum of Patterns");
    }

    @Test
    @DisplayName("The entering trigger gains life for its controller even after Pendulum is sacrificed")
    void enteringTriggerSurvivesSacrifice() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        gd.activePlayerId = player2.getId();
        harness.setLibrary(player2, List.of(new PendulumOfPatterns()));
        harness.castFromHand(player2, new PendulumOfPatterns(), "{2}");
        harness.passBothPriorities();
        harness.assertLife(player2, 10);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player2, 10);
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player2, "Pendulum of Patterns");
    }
}
