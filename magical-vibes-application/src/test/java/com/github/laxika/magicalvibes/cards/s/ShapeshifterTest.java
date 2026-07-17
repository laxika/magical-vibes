package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShapeshifterTest extends BaseCardTest {

    private Permanent castAndChoose(String chosenNumber) {
        harness.setHand(player1, List.of(new Shapeshifter()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        if (chosenNumber != null) {
            harness.handleListChoice(player1, chosenNumber);
        }
        return findPermanent(player1, "Shapeshifter");
    }

    @Test
    @DisplayName("Entering awaits a number choice")
    void enteringAwaitsNumberChoice() {
        castAndChoose(null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOf(com.github.laxika.magicalvibes.model.ChoiceContext.NumberChoice.class);
    }

    @Test
    @DisplayName("Choosing 3 makes it a 3/4")
    void choosingThree() {
        Permanent shifter = castAndChoose("3");

        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shifter)).isEqualTo(4);
    }

    @Test
    @DisplayName("Choosing 0 makes it a 0/7")
    void choosingZero() {
        Permanent shifter = castAndChoose("0");

        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, shifter)).isEqualTo(7);
    }

    @Test
    @DisplayName("Upkeep re-choice updates power and toughness")
    void upkeepReChoice() {
        Permanent shifter = harness.addToBattlefieldAndReturn(player1, new Shapeshifter());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, "you may choose" trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability → MayEffect prompts
        harness.handleMayAbilityChosen(player1, true); // accept → number choice begins
        harness.handleListChoice(player1, "5");

        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, shifter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the upkeep choice keeps the last chosen number")
    void decliningUpkeepKeepsNumber() {
        Permanent shifter = harness.addToBattlefieldAndReturn(player1, new Shapeshifter());

        // First upkeep: pick 5 (5/2).
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "5");

        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(5);

        // Second upkeep: decline — the 5/2 body is unchanged.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, shifter)).isEqualTo(2);
    }
}
