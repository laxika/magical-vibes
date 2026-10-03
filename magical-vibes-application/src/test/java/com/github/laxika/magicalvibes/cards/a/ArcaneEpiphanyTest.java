package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcaneEpiphany.class, AetherAdept.class, RuneclawBear.class})
class ArcaneEpiphanyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving draws three cards")
    void drawsThreeCards() {
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear(), new RuneclawBear()));
        harness.castFromHand(player1, new ArcaneEpiphany(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Arcane Epiphany");
    }

    @Test
    @DisplayName("Costs one less with a Wizard")
    void costsOneLessWithWizard() {
        harness.addToBattlefield(player1, new AetherAdept());
        harness.castFromHand(player1, new ArcaneEpiphany(), "{2}{U}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot cast for four mana without a Wizard")
    void cannotCastForFourManaWithoutWizard() {
        harness.setHand(player1, List.of(new ArcaneEpiphany()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void opposingWizardDoesNotReduceCost() {
        harness.addToBattlefield(player2, new AetherAdept());

        assertThatThrownBy(() -> harness.castFromHand(player1, new ArcaneEpiphany(), "{2}{U}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void nonWizardCreatureDoesNotReduceCost() {
        harness.addToBattlefield(player1, new RuneclawBear());

        assertThatThrownBy(() -> harness.castFromHand(player1, new ArcaneEpiphany(), "{2}{U}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void multipleWizardsOnlyReduceCostOnce() {
        harness.addToBattlefield(player1, new AetherAdept());
        harness.addToBattlefield(player1, new AetherAdept());

        harness.castFromHand(player1, new ArcaneEpiphany(), "{3}{U}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void reductionDoesNotReplaceRequiredBlueMana() {
        harness.addToBattlefield(player1, new AetherAdept());

        assertThatThrownBy(() -> harness.castFromHand(player1, new ArcaneEpiphany(), "{3}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
