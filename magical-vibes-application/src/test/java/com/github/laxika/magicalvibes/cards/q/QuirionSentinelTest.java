package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(QuirionSentinel.class)
class QuirionSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("When Quirion Sentinel enters, its controller chooses a color and gets one mana")
    void enteringAddsOneManaOfChosenColor() {
        harness.castFromHand(player1, new QuirionSentinel(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Quirion Sentinel");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ETB ability offers each colored choice but not colorless")
    void offersOnlyColoredChoices() {
        harness.castFromHand(player1, new QuirionSentinel(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ETB trigger still resolves after Quirion Sentinel leaves")
    void triggerResolvesAfterSourceLeaves() {
        harness.castFromHand(player1, new QuirionSentinel(), "{1}{G}");
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
