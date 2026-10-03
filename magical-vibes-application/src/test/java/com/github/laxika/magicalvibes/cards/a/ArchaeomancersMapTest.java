package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchaeomancersMap.class, Forest.class, GrizzlyBears.class, Plains.class})
class ArchaeomancersMapTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and searches for up to two basic Plains cards")
    void entersAndSearchesForBasicPlains() {
        Plains firstPlains = new Plains();
        Plains secondPlains = new Plains();
        Forest forest = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstPlains, forest, secondPlains, creature));

        harness.enterBattlefieldAndReturn(player1, new ArchaeomancersMap());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(firstPlains, secondPlains);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(firstPlains, secondPlains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, creature);
    }

    @Test
    @DisplayName("Offers the optional land put only when the opponent has more lands")
    void opponentLandOffersLandFromHand() {
        harness.addToBattlefield(player1, new ArchaeomancersMap());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Plains());
        harness.enterBattlefieldAndReturn(player2, new Forest());
        Forest landInHand = new Forest();
        harness.setHand(player1, List.of(landInHand));

        harness.enterBattlefieldAndReturn(player2, new Forest());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == landInHand);
    }

    @Test
    @DisplayName("Does not trigger when the opponent has no land-count advantage")
    void noTriggerWithoutLandCountAdvantage() {
        harness.addToBattlefield(player1, new ArchaeomancersMap());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Checks the land-count condition again when the trigger resolves")
    void doesNothingIfLandCountsEqualizeBeforeResolution() {
        harness.addToBattlefield(player1, new ArchaeomancersMap());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Plains());
        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player2, new Forest());
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new Plains());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("May find no Plains even when two are available")
    void mayFindZeroPlains() {
        Plains first = new Plains();
        Plains second = new Plains();
        harness.setLibrary(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new ArchaeomancersMap());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("May stop searching after finding one Plains")
    void mayFindOnlyOnePlains() {
        Plains first = new Plains();
        Plains second = new Plains();
        harness.setLibrary(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new ArchaeomancersMap());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Search finishes when there are no basic Plains")
    void searchWithoutMatchingCards() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.enterBattlefieldAndReturn(player1, new ArchaeomancersMap());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Controller may decline to put a land onto the battlefield")
    void mayDeclineLandFromHand() {
        harness.addToBattlefield(player1, new ArchaeomancersMap());
        Forest land = new Forest();
        harness.setHand(player1, List.of(land));
        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Controller's own land entering does not trigger the Map")
    void ownLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArchaeomancersMap());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Plains()));

        harness.enterBattlefieldAndReturn(player1, new Plains());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting with no land in hand does not put a creature onto the battlefield")
    void noLandInHand() {
        harness.addToBattlefield(player1, new ArchaeomancersMap());
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
