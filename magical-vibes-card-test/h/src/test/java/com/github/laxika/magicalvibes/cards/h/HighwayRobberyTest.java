package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighwayRobbery.class, Forest.class})
class HighwayRobberyTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card draws two cards")
    void discardingDrawsTwoCards() {
        Forest discarded = new Forest();
        Forest drawnOne = new Forest();
        Forest drawnTwo = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new HighwayRobbery(), discarded)));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        addMana();

        castAndAccept();
        harness.handleListChoice(player1, "Discard a card");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
    }

    @Test
    @DisplayName("Sacrificing a chosen land draws two cards")
    void sacrificingLandDrawsTwoCards() {
        Forest drawnOne = new Forest();
        Forest drawnTwo = new Forest();
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new HighwayRobbery()));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        addMana();

        castAndAccept();
        harness.handleListChoice(player1, "Sacrifice a land");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(secondLand.getId()));

        assertThat(secondLand).isNotIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(firstLand).isIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
    }

    @Test
    @DisplayName("Declining the optional action does nothing")
    void declineDoesNothing() {
        Forest inHand = new Forest();
        Forest onTop = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new HighwayRobbery(), inHand)));
        harness.setLibrary(player1, List.of(onTop));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(inHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(inHand);
    }

    @Test
    @DisplayName("An empty hand cannot produce the discard draw")
    void emptyHandDoesNotDraw() {
        Forest onTop = new Forest();
        harness.setLibrary(player1, List.of(onTop));
        harness.castFromHand(player1, new HighwayRobbery(), "{1}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Discard a card");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onTop);
    }

    @Test
    @DisplayName("An opponent's land cannot pay for the draw")
    void noControlledLandDoesNotDraw() {
        Forest onTop = new Forest();
        Permanent opponentsLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player1, List.of(onTop));
        harness.castFromHand(player1, new HighwayRobbery(), "{1}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Sacrifice a land");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onTop);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentsLand);
    }

    @Test
    @DisplayName("Sacrificing the only controlled land draws without a selection prompt")
    void sacrificingOnlyLandDrawsTwoCards() {
        Forest sacrificed = new Forest();
        Forest drawnOne = new Forest();
        Forest drawnTwo = new Forest();
        harness.addToBattlefield(player1, sacrificed);
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        harness.castFromHand(player1, new HighwayRobbery(), "{1}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Sacrifice a land");

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
    }

    @Test
    @DisplayName("Plot delays casting until a later turn and then allows a free cast")
    void plotThenCastOnLaterTurn() {
        HighwayRobbery robbery = new HighwayRobbery();
        Forest discarded = new Forest();
        Forest drawnOne = new Forest();
        Forest drawnTwo = new Forest();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, new ArrayList<>(List.of(robbery, discarded)));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        addMana();

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(robbery);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.castFromExile(player1, robbery.getId()))
                .isInstanceOf(IllegalStateException.class);

        gd.turnNumber += 2;
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, robbery.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, robbery.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Discard a card");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(robbery);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(robbery, discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
    }

    private void castAndAccept() {
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
