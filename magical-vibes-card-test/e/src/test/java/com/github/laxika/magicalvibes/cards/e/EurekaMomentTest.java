package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.q.QuandrixPledgemage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EurekaMoment.class, Forest.class, QuandrixPledgemage.class})
class EurekaMomentTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then may put a land from hand onto the battlefield")
    void drawsTwoCardsThenMayPutLandOntoBattlefield() {
        Card firstDraw = new QuandrixPledgemage();
        Card secondDraw = new QuandrixPledgemage();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new EurekaMoment(), new Forest(), new QuandrixPledgemage()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice handChoice =
                gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(handChoice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Quandrix Pledgemage");
    }

    @Test
    @DisplayName("Declining to put a land leaves it in hand after drawing two cards")
    void decliningLandPutLeavesLandInHand() {
        Card firstDraw = new QuandrixPledgemage();
        Card secondDraw = new QuandrixPledgemage();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new EurekaMoment(), new Forest()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("A land drawn by the spell can be put onto the battlefield")
    void canPutNewlyDrawnLandOntoBattlefield() {
        Card drawnLand = new Forest();
        Card secondDraw = new QuandrixPledgemage();
        harness.setLibrary(player1, List.of(drawnLand, secondDraw));
        harness.setHand(player1, List.of(new EurekaMoment()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnLand, secondDraw);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDraw);
        harness.assertInGraveyard(player1, "Eureka Moment");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting with no land in hand still completes the spell after drawing")
    void noLandInHandCompletesResolution() {
        Card firstDraw = new QuandrixPledgemage();
        Card secondDraw = new QuandrixPledgemage();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new EurekaMoment()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Eureka Moment");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Puts only one land even after the normal land play has been used")
    void putsOneLandAfterNormalLandPlay() {
        Card firstDraw = new QuandrixPledgemage();
        Card secondDraw = new QuandrixPledgemage();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new Forest(), new EurekaMoment(), new Forest(), new Forest()));
        harness.playLand(player1, 0);
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).contains(firstDraw, secondDraw);
        harness.assertInGraveyard(player1, "Eureka Moment");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
