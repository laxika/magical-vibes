package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.DanithaCapashenParagon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrunnTheLonelyKing;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsAmongControlledEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoxAmberTest extends BaseCardTest {

    @Test
    @DisplayName("Has tap activated ability with AwardManaOfColorsAmongControlledEffect")
    void hasCorrectAbility() {
        MoxAmber card = new MoxAmber();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(AwardManaOfColorsAmongControlledEffect.class);
        assertThat(ability.getTimingRestriction()).isNull();
    }

    @Test
    @DisplayName("Produces no mana when no legendary creatures or planeswalkers are controlled")
    void producesNoManaWithoutLegendaries() {
        harness.addToBattlefield(player1, new MoxAmber());
        harness.addToBattlefield(player1, new GrizzlyBears());

        int whiteBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE);
        int greenBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);

        // No legendary creatures or planeswalkers — no mana produced, no color choice
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(whiteBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(greenBefore);
    }

    @Test
    @DisplayName("Auto-adds mana when only one color is available from a single legendary creature")
    void autoAddsManaWithSingleColor() {
        harness.addToBattlefield(player1, new MoxAmber());
        harness.addToBattlefield(player1, new DanithaCapashenParagon());

        int whiteBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE);

        harness.activateAbility(player1, 0, null, null);

        // Only white legendary creature — auto-adds white mana, no choice needed
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(whiteBefore + 1);
    }

    @Test
    @DisplayName("Prompts for color choice when multiple colors are available")
    void promptsForChoiceWithMultipleColors() {
        harness.addToBattlefield(player1, new MoxAmber());
        harness.addToBattlefield(player1, new DanithaCapashenParagon()); // white
        harness.addToBattlefield(player1, new GrunnTheLonelyKing()); // green

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.colorChoice().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color from available legendary colors adds the correct mana")
    void choosingColorAddsMana() {
        harness.addToBattlefield(player1, new MoxAmber());
        harness.addToBattlefield(player1, new DanithaCapashenParagon()); // white
        harness.addToBattlefield(player1, new GrunnTheLonelyKing()); // green

        harness.activateAbility(player1, 0, null, null);

        int greenBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(greenBefore + 1);
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Planeswalkers provide their colors for mana production")
    void planeswalkerProvidesColors() {
        harness.addToBattlefield(player1, new MoxAmber());
        harness.addToBattlefield(player1, new ChandraNalaar()); // red planeswalker

        int redBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, null, null);

        // Only red planeswalker — auto-adds red mana
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redBefore + 1);
    }

    @Test
    @DisplayName("Non-legendary creatures do not contribute colors")
    void nonLegendaryCreatureDoesNotContribute() {
        harness.addToBattlefield(player1, new MoxAmber());
        harness.addToBattlefield(player1, new GrizzlyBears()); // non-legendary green creature

        harness.activateAbility(player1, 0, null, null);

        // Non-legendary creature should not count — no mana produced
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new MoxAmber());
        harness.addToBattlefield(player1, new DanithaCapashenParagon());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Opponent's legendary creatures do not contribute colors")
    void opponentLegendariesDoNotContribute() {
        harness.addToBattlefield(player1, new MoxAmber());
        harness.addToBattlefield(player2, new DanithaCapashenParagon()); // opponent's legendary

        harness.activateAbility(player1, 0, null, null);

        // Opponent's legendary creature should not count
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }
}
