package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartelAristocratTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature grants protection from the chosen color")
    void grantsProtectionFromChosenColor() {
        Permanent aristocrat = addAristocratReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(aristocrat.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionClearedAtEndOfTurn() {
        Permanent aristocrat = addAristocratReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        assertThat(aristocrat.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.WHITE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aristocrat.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.WHITE);
    }

    @Test
    @DisplayName("Cannot activate when Cartel Aristocrat is the only creature")
    void cannotSacrificeItself() {
        addAristocratReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Cartel Aristocrat");
    }

    @Test
    @DisplayName("Choosing Cartel Aristocrat itself as the sacrifice is rejected")
    void choosingItselfIsRejected() {
        Permanent aristocrat = addAristocratReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, aristocrat.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Cartel Aristocrat");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("The chosen creature is the one sacrificed")
    void chosenCreatureIsSacrificed() {
        Permanent aristocrat = addAristocratReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        UUID giantId = harness.getPermanentId(player1, "Hill Giant");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(aristocrat.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
    }

    private Permanent addAristocratReady(Player player) {
        Permanent perm = new Permanent(new CartelAristocrat());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
