package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Shapeshifter.class)
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

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isInstanceOf(ChoiceContext.NumberChoice.class);
        assertThat(choice.options()).containsExactly("0", "1", "2", "3", "4", "5", "6", "7");
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
    @DisplayName("Choosing 7 makes it 7/0 and puts it into the graveyard")
    void choosingSevenPutsItIntoGraveyard() {
        castAndChoose(null);
        harness.handleListChoice(player1, "7");

        harness.assertNotOnBattlefield(player1, "Shapeshifter");
        harness.assertInGraveyard(player1, "Shapeshifter");
    }

    @Test
    @DisplayName("A number outside the offered range is rejected")
    void rejectsOutOfRangeNumberChoice() {
        castAndChoose(null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "8"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Upkeep re-choice updates power and toughness")
    void upkeepReChoice() {
        Permanent shifter = harness.addToBattlefieldAndReturn(player1, new Shapeshifter());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "5");

        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, shifter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the upkeep choice keeps the last chosen number")
    void decliningUpkeepKeepsNumber() {
        Permanent shifter = harness.addToBattlefieldAndReturn(player1, new Shapeshifter());

        // First upkeep: pick 5 (5/2).
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "5");

        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(5);

        // Second upkeep: decline - the 5/2 body is unchanged.
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, shifter)).isEqualTo(2);
    }
}
