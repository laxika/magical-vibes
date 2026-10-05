package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.Abundance;
import com.github.laxika.magicalvibes.cards.c.CanoptekWraith;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OutOfTheTombs.class, CanoptekWraith.class, Island.class, Abundance.class, GrafdiggersCage.class})
class OutOfTheTombsTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep adds two eon counters, then mills equal to the new total")
    void upkeepAddsCountersAndMills() {
        Permanent tombs = harness.addToBattlefieldAndReturn(player1, new OutOfTheTombs());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));

        triggerUpkeep(player1);

        assertThat(tombs.getCounterCount(CounterType.EON)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An empty-library draw returns a creature from the graveyard to the battlefield")
    void emptyLibraryDrawReturnsCreature() {
        harness.addToBattlefield(player1, new OutOfTheTombs());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new Island(), new CanoptekWraith()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Canoptek Wraith");
        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("An empty-library draw loses when the graveyard has no creature card")
    void emptyLibraryDrawLosesWithoutCreature() {
        harness.addToBattlefield(player1, new OutOfTheTombs());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new Island()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Draws normally while the library has cards")
    void drawsNormallyWithCardsInLibrary() {
        harness.addToBattlefield(player1, new OutOfTheTombs());
        harness.setLibrary(player1, List.of(new Island()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void upkeepUsesExistingCountersAndMillsOnlyAvailableCards() {
        Permanent tombs = harness.addToBattlefieldAndReturn(player1, new OutOfTheTombs());
        tombs.setCounterCount(CounterType.EON, 4);
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));

        triggerUpkeep(player1);

        assertThat(tombs.getCounterCount(CounterType.EON)).isEqualTo(6);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void opponentsUpkeepDoesNotAddCountersOrMill() {
        Permanent tombs = harness.addToBattlefieldAndReturn(player1, new OutOfTheTombs());
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        triggerUpkeep(player2);

        assertThat(tombs.getCounterCount(CounterType.EON)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void eachDrawReturnsOneCreatureThenLosesWhenNoneRemain() {
        harness.addToBattlefield(player1, new OutOfTheTombs());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new CanoptekWraith(), new CanoptekWraith()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 3));
        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof CanoptekWraith)).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void opponentsEmptyLibraryDrawIsNotReplaced() {
        harness.addToBattlefield(player1, new OutOfTheTombs());
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player2, List.of(new CanoptekWraith()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        harness.assertNotOnBattlefield(player2, "Canoptek Wraith");
    }

    @Test
    void cageMakesReturningACreatureImpossibleAndCausesLoss() {
        harness.addToBattlefield(player1, new OutOfTheTombs());
        harness.addToBattlefield(player2, new GrafdiggersCage());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new CanoptekWraith()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Canoptek Wraith");
        harness.assertNotOnBattlefield(player1, "Canoptek Wraith");
    }

    @Test
    void abundanceCanReplaceEmptyLibraryDrawInsteadOfLosing() {
        harness.addToBattlefield(player1, new OutOfTheTombs());
        harness.addToBattlefield(player1, new Abundance());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "LAND");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playersAttemptedDrawFromEmptyLibrary).doesNotContain(player1.getId());
    }

    @Test
    void losingAbilitiesDisablesTheDrawReplacement() {
        Permanent tombs = harness.addToBattlefieldAndReturn(player1, new OutOfTheTombs());
        tombs.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new CanoptekWraith()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        harness.assertNotOnBattlefield(player1, "Canoptek Wraith");
    }

    private void triggerUpkeep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();
    }
}
