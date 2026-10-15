package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NyxbornBrute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.t.ThrillOfPossibility;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AllureOfTheUnknown.class, Forest.class, NyxbornBrute.class, ThrillOfPossibility.class})
class AllureOfTheUnknownTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent exiles a revealed nonland, the rest go to hand, and they may cast it free")
    void opponentExilesNonlandAndMayCastIt() {
        Forest forest1 = new Forest();
        NyxbornBrute firstBear = new NyxbornBrute();
        Forest forest2 = new Forest();
        NyxbornBrute chosenBear = new NyxbornBrute();
        Forest forest3 = new Forest();
        Forest forest4 = new Forest();
        NyxbornBrute untouched = new NyxbornBrute();
        setLibrary(forest1, firstBear, forest2, chosenBear, forest3, forest4, untouched);

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(firstBear.getId(), chosenBear.getId());

        harness.handleMultipleCardsChosen(player2, List.of(chosenBear.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosenBear);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(forest1, firstBear, forest2, forest3, forest4);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Nyxborn Brute");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Allure of the Unknown");
    }

    @Test
    @DisplayName("If the revealed cards are all lands, they all go to the controller's hand")
    void allLandsGoToHandWithoutAnOpponentChoice() {
        Forest forest1 = new Forest();
        Forest forest2 = new Forest();
        Forest forest3 = new Forest();
        Forest forest4 = new Forest();
        Forest forest5 = new Forest();
        Forest forest6 = new Forest();
        NyxbornBrute untouched = new NyxbornBrute();
        setLibrary(forest1, forest2, forest3, forest4, forest5, forest6, untouched);

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(forest1, forest2, forest3, forest4, forest5, forest6);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    @Test
    @DisplayName("Declining the cast leaves the chosen card only in exile")
    void decliningLeavesCardExiledWithoutDuplicatingRevealedCards() {
        NyxbornBrute chosen = new NyxbornBrute();
        Forest rest = new Forest();
        setLibrary(chosen, rest);
        harness.setLibrary(player2, List.of());

        castAndResolve();
        harness.handleMultipleCardsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(rest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Allure of the Unknown");
    }

    @Test
    @DisplayName("An empty library reveals nothing and does not cause a draw loss")
    void emptyLibraryDoesNothing() {
        setLibrary();

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Allure of the Unknown");
    }

    @Test
    @DisplayName("A free spell cannot be cast if its mandatory discard cost cannot be paid")
    void cannotCastThrillWithoutACardToDiscard() {
        ThrillOfPossibility chosen = new ThrillOfPossibility();
        setLibrary(chosen);
        harness.setHand(player2, List.of());

        castAndResolve();
        harness.handleMultipleCardsChosen(player2, List.of(chosen.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Allure of the Unknown");
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new AllureOfTheUnknown(), "{3}{B}{R}");
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
