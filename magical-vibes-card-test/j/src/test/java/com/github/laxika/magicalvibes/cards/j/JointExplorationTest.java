package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AcademyWall;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({JointExploration.class, Forest.class, AcademyWall.class})
class JointExplorationTest extends BaseCardTest {

    @Test
    @DisplayName("Scries two, then draws a card without kicker")
    void scriesThenDrawsWithoutKicker() {
        Card first = new Forest();
        Card second = new AcademyWall();
        Card draw = new AcademyWall();
        harness.setLibrary(player1, List.of(first, second, draw));
        harness.setHand(player1, List.of(new JointExploration()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw, first);
    }

    @Test
    @DisplayName("A kicked cast may put a land from hand onto the battlefield")
    void kickedCastMayPutLandOntoBattlefield() {
        Card first = new AcademyWall();
        Card second = new Forest();
        Card draw = new AcademyWall();
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
        Card first = new AcademyWall();
        Card second = new AcademyWall();
        Card draw = new AcademyWall();
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

    @Test
    @DisplayName("A kicked spell can put the land it just drew onto the battlefield")
    void canPutNewlyDrawnLandOntoBattlefield() {
        Forest drawnLand = new Forest();
        Forest otherLand = new Forest();
        Card nonland = new AcademyWall();
        harness.setLibrary(player1, List.of(drawnLand, nonland));
        harness.setHand(player1, List.of(new JointExploration(), otherLand));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(otherLand, drawnLand);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard()).isSameAs(drawnLand);
            assertThat(permanent.isTapped()).isFalse();
        });
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(otherLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Joint Exploration");
    }

    @Test
    @DisplayName("Accepting the kicked option with no land in hand finishes without putting a nonland")
    void noLandInHandDoesNotPutNonlandOntoBattlefield() {
        Card first = new AcademyWall();
        Card second = new AcademyWall();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new JointExploration()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Joint Exploration");
    }

    @Test
    @DisplayName("An unkicked spell with a one-card library draws but cannot put a land")
    void unkickedWithOneCardLibraryCannotPutLand() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new JointExploration()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Joint Exploration");
    }
}
