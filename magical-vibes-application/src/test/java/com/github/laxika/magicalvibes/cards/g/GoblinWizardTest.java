package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinWizard.class, GoblinHero.class, GrizzlyBears.class})
class GoblinWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Goblin Wizard offers a Goblin permanent from hand")
    void tappingOffersGoblinPermanent() {
        Permanent wizard = addCreatureReady(player1, new GoblinWizard());
        harness.setHand(player1, List.of(new GoblinHero(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(wizard.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Goblin Hero");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The second ability gives a Goblin protection from white until end of turn")
    void grantsProtectionFromWhite() {
        addCreatureReady(player1, new GoblinWizard());
        Permanent goblin = addCreatureReady(player1, new GoblinHero());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(goblin.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.WHITE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(goblin.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.WHITE);
    }

    @Test
    @DisplayName("The second ability cannot target a non-Goblin creature")
    void cannotTargetNonGoblinCreature() {
        addCreatureReady(player1, new GoblinWizard());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
