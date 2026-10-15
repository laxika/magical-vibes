package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(RelicOfSauron.class)
class RelicOfSauronTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds two mana in the chosen combination of blue, black, and red")
    void addsTwoManaInChosenCombination() {
        harness.addToBattlefield(player1, new RelicOfSauron());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying three mana draws two cards, then prompts for one discard")
    void drawsTwoThenDiscardsOne() {
        harness.addToBattlefield(player1, new RelicOfSauron());
        RelicOfSauron initialCard = new RelicOfSauron();
        RelicOfSauron firstDraw = new RelicOfSauron();
        RelicOfSauron secondDraw = new RelicOfSauron();
        harness.setHand(player1, List.of(initialCard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(initialCard, firstDraw, secondDraw);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(initialCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw ability requires three mana")
    void drawAbilityRequiresThreeMana() {
        harness.addToBattlefield(player1, new RelicOfSauron());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @CsvSource({"BLUE, BLUE", "BLACK, BLACK", "RED, RED", "BLUE, BLACK", "BLUE, RED", "BLACK, RED"})
    void allowsEveryManaCombination(ManaColor first, ManaColor second) {
        harness.addToBattlefield(player1, new RelicOfSauron());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, first.name());
        harness.handleListChoice(player1, second.name());

        for (ManaColor color : ManaColor.values()) {
            int expected = (color == first ? 1 : 0) + (color == second ? 1 : 0);
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(expected);
        }
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canDiscardANewlyDrawnCardStartingWithAnEmptyHand() {
        harness.addToBattlefield(player1, new RelicOfSauron());
        RelicOfSauron firstDraw = new RelicOfSauron();
        RelicOfSauron secondDraw = new RelicOfSauron();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(secondDraw);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void tappingForManaPreventsActivatingEitherAbilityAgain() {
        harness.addToBattlefield(player1, new RelicOfSauron());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLACK");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
