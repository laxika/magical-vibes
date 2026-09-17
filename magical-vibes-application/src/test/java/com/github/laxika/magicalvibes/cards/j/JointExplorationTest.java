package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JointExploration.class, Forest.class, GrizzlyBears.class})
class JointExplorationTest extends BaseCardTest {

    @Test
    @DisplayName("Scries two, then draws a card without kicker")
    void scriesThenDrawsWithoutKicker() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, draw));
        harness.setHand(player1, List.of(new JointExploration()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw, first);
    }

    @Test
    @DisplayName("A kicked cast may put a land from hand onto the battlefield")
    void kickedCastMayPutLandOntoBattlefield() {
        Card first = new GrizzlyBears();
        Card second = new Forest();
        Card draw = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(first, second, draw));
        harness.setHand(player1, List.of(new JointExploration(), land));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, first);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Declining the kicked land put leaves the land in hand")
    void decliningKickedLandPutLeavesLandInHand() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card draw = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(first, second, draw));
        harness.setHand(player1, List.of(new JointExploration(), land));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, first);
        harness.assertNotOnBattlefield(player1, "Forest");
    }
}
