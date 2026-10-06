package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LlanowarTribe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.PendingInteraction;




@CardUsed({SeasonedPyromancer.class, SnowCoveredForest.class, LlanowarTribe.class})
class SeasonedPyromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates one Elemental for one nonland card discarded")
    void enteringCreatesTokensForNonlandDiscards() {
        harness.setHand(player1, List.of(new LlanowarTribe(), new SnowCoveredForest()));
        harness.setLibrary(player1, List.of(new SnowCoveredForest(), new LlanowarTribe()));
        harness.enterBattlefieldAndReturn(player1, new SeasonedPyromancer());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Tribe");
        harness.assertInGraveyard(player1, "Snow-Covered Forest");
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Snow-Covered Forest", "Llanowar Tribe");
    }

    @Test
    @DisplayName("Exiling it from the graveyard creates two Elementals")
    void graveyardAbilityCreatesTwoTokens() {
        harness.setGraveyard(player1, List.of(new SeasonedPyromancer()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).hasSize(2);
        harness.assertNotInGraveyard(player1, "Seasoned Pyromancer");
    }

    @Test
    void emptyHandStillDrawsTwoWithoutCreatingTokens() {
        SnowCoveredForest first = new SnowCoveredForest();
        LlanowarTribe second = new LlanowarTribe();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new SeasonedPyromancer());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(countPermanents(player1, "Elemental")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void singleLandIsDiscardedAndStillDrawsTwoWithoutTokens() {
        SnowCoveredForest discarded = new SnowCoveredForest();
        SnowCoveredForest first = new SnowCoveredForest();
        LlanowarTribe second = new LlanowarTribe();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new SeasonedPyromancer());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(countPermanents(player1, "Elemental")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void singleNonlandCreatesTokenDuringTheOriginalResolution() {
        LlanowarTribe discarded = new LlanowarTribe();
        SnowCoveredForest first = new SnowCoveredForest();
        SnowCoveredForest second = new SnowCoveredForest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new SeasonedPyromancer());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void twoNonlandsCreateBothTokensBeforePriorityReturns() {
        LlanowarTribe firstDiscard = new LlanowarTribe();
        LlanowarTribe secondDiscard = new LlanowarTribe();
        SnowCoveredForest firstDraw = new SnowCoveredForest();
        SnowCoveredForest secondDraw = new SnowCoveredForest();
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.enterBattlefieldAndReturn(player1, new SeasonedPyromancer());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDiscard, secondDiscard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void twoLandsCreateNoTokens() {
        SnowCoveredForest firstDiscard = new SnowCoveredForest();
        SnowCoveredForest secondDiscard = new SnowCoveredForest();
        LlanowarTribe firstDraw = new LlanowarTribe();
        LlanowarTribe secondDraw = new LlanowarTribe();
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.enterBattlefieldAndReturn(player1, new SeasonedPyromancer());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDiscard, secondDiscard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(countPermanents(player1, "Elemental")).isZero();
        assertThat(gd.stack).isEmpty();
    }
}

@CardUsed({SeasonedPyromancer.class, SnowCoveredForest.class, LlanowarTribe.class})
class Mh1SeasonedPyromancerTest extends BaseCardTest {

    @Test
    void entersDiscardsTwoDrawsTwoAndCreatesElementalsForNonlands() {
        SnowCoveredForest discardedLand = new SnowCoveredForest();
        LlanowarTribe discardedNonland = new LlanowarTribe();
        SnowCoveredForest drawnOne = new SnowCoveredForest();
        LlanowarTribe drawnTwo = new LlanowarTribe();
        harness.setHand(player1, List.of(new SeasonedPyromancer(), discardedLand, discardedNonland));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(discardedLand, discardedNonland);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
    }

    @Test
    void graveyardAbilityExilesSourceAndCreatesTwoElementals() {
        SeasonedPyromancer pyromancer = new SeasonedPyromancer();
        harness.setGraveyard(player1, List.of(pyromancer));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Seasoned Pyromancer");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(pyromancer);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);
    }
}
