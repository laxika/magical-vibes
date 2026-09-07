package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SehtsTiger.class, GrizzlyBears.class, Shock.class})
class SehtsTigerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB lets its controller choose a color for protection")
    void grantsControllerProtectionFromChosenColor() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SehtsTiger()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.get(player1.getId()))
                .contains(CardColor.RED);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> assertThat(permanent.getProtectionFromColorsUntilEndOfTurn())
                        .doesNotContain(CardColor.RED));
    }

    @Test
    @DisplayName("Protection from the chosen color prevents that color from targeting its controller")
    void chosenColorProtectionPreventsTargetingController() {
        harness.setHand(player1, List.of(new SehtsTiger()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Controller protection expires at end of turn")
    void controllerProtectionExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new SehtsTiger()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.get(player1.getId()))
                .contains(CardColor.BLUE);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn
                .getOrDefault(player1.getId(), new HashSet<>()))
                .doesNotContain(CardColor.BLUE);
    }
}
