package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KangPrime.class, Forest.class, Island.class, GrizzlyBears.class})
class KangPrimeTest extends BaseCardTest {

    @Test
    @DisplayName("The library dig stops at the first nonland card")
    void leavesCardsAfterFirstNonlandInLibrary() {
        Island land = new Island();
        GrizzlyBears firstNonland = new GrizzlyBears();
        GrizzlyBears nextCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, firstNonland, nextCard));

        harness.enterBattlefieldAndReturn(player1, new KangPrime());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.exiledCardTimeCounters).containsOnlyKeys(firstNonland.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, firstNonland);
    }

    @Test
    @DisplayName("An empty library does not create a suspended card")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new KangPrime());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exiledCardTimeCounters).isEmpty();
    }

    @Test
    @DisplayName("Time counters are removed only during the card owner's upkeep")
    void opponentsUpkeepDoesNotRemoveTimeCounter() {
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonland));
        harness.enterBattlefieldAndReturn(player1, new KangPrime());
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(nonland.getId(), 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(nonland.getId(), 1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the suspend cast leaves the card exiled without another offer next upkeep")
    void decliningCastLeavesCardExiled() {
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonland));
        harness.enterBattlefieldAndReturn(player1, new KangPrime());
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(nonland);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(nonland.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature cast through suspend can attack immediately")
    void suspendedCreatureGainsHaste() {
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonland));
        harness.enterBattlefieldAndReturn(player1, new KangPrime());
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        declareAttackers(List.of(1));

        assertThat(findPermanent(player1, "Grizzly Bears").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Entering exiles through the first nonland card and suspends it")
    void enteringExilesUntilNonlandAndSuspendsIt() {
        Island land = new Island();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, nonland));

        harness.enterBattlefieldAndReturn(player1, new KangPrime());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .containsExactly(land.getId(), nonland.getId());
        assertThat(gd.exiledCardTimeCounters).containsEntry(nonland.getId(), 2);
    }

    @Test
    @DisplayName("Attacking repeats the library dig and suspends the first nonland card")
    void attackingExilesUntilNonlandAndSuspendsIt() {
        Island land = new Island();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, nonland));
        addCreatureReady(player1, new KangPrime());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .containsExactly(land.getId(), nonland.getId());
        assertThat(gd.exiledCardTimeCounters).containsEntry(nonland.getId(), 2);
    }

    @Test
    @DisplayName("No nonland card means no suspended card is registered")
    void noNonlandCardDoesNotRegisterSuspend() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        harness.enterBattlefieldAndReturn(player1, new KangPrime());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .containsExactly(first.getId(), second.getId());
        assertThat(gd.exiledCardTimeCounters).isEmpty();
    }

    @Test
    @DisplayName("The suspended card counts down and can be cast for free")
    void suspendedCardCanBeCastForFree() {
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonland));
        harness.enterBattlefieldAndReturn(player1, new KangPrime());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(nonland.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
