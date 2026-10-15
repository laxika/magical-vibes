package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesertCenote.class, GiantGrowth.class, DoomBlade.class})
class DesertCenoteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped for the starting player")
    void entersTappedForStartingPlayer() {
        harness.setHand(player1, List.of(new DesertCenote()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Desert Cenote").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped for a player who was not the starting player")
    void entersUntappedForNonStartingPlayer() {
        harness.forceActivePlayer(player2);
        gd.startingPlayerId = player1.getId();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DesertCenote()));

        harness.playLand(player2, 0);

        assertThat(findPermanent(player2, "Desert Cenote").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Chooses up to two colors represented by cards in hand")
    void choosesUpToTwoColorsFromHand() {
        harness.setHand(player1, List.of(new DesertCenote(), new GiantGrowth(), new DoomBlade()));

        harness.playLand(player1, 0);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("BLACK", "GREEN", "DONE");

        harness.handleListChoice(player1, "BLACK");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("GREEN", "DONE");
        harness.handleListChoice(player1, "DONE");

        Permanent cenote = findPermanent(player1, "Desert Cenote");
        assertThat(cenote.getChosenColors()).containsExactly(CardColor.BLACK);
        assertThat(cenote.getChosenColor()).isEqualTo(CardColor.BLACK);
    }

    @Test
    @DisplayName("The chosen-color ability uses either selected color")
    void chosenColorAbilityUsesSelectedColors() {
        harness.setHand(player1, List.of(new DesertCenote(), new GiantGrowth(), new DoomBlade()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        Permanent cenote = findPermanent(player1, "Desert Cenote");
        assertThat(cenote.getChosenColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(cenote.getChosenColor()).isNull();

        cenote.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("BLACK", "GREEN");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
    }
}
