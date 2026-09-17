package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
