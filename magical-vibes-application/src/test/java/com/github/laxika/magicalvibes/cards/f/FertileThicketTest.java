package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FertileThicket.class, Forest.class, GrizzlyBears.class, Plains.class})
class FertileThicketTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and may put a revealed basic land on top")
    void entersTappedAndPutsRevealedBasicLandOnTop() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears, plains));

        playFertileThicket(player1);

        assertThat(findPermanent(player1, "Fertile Thicket").isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest, plains);
        assertThat(search.params().reveals()).isTrue();

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains, bears, forest);
    }

    @Test
    @DisplayName("Declining the enters-the-battlefield search leaves the library unchanged")
    void decliningSearchLeavesLibraryUnchanged() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));

        playFertileThicket(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, bears);
    }

    @Test
    @DisplayName("Produces green mana after entering")
    void producesGreenMana() {
        harness.addToBattlefield(player1, new FertileThicket());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN))
                .isEqualTo(1);
    }

    private void playFertileThicket(Player player) {
        harness.setHand(player, List.of(new FertileThicket()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player, 0);
    }
}
