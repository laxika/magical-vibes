package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DuskLegionZealot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TombRobber.class, Forest.class, DuskLegionZealot.class})
class TombRobberTest extends BaseCardTest {

    @Test
    @DisplayName("Activating with a land on top puts it into hand")
    void exploresLand() {
        Card land = new Forest();
        addTombRobber(land);
        harness.setHand(player1, List.of(new DuskLegionZealot()));

        activateAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(findPermanent(player1, "Tomb Robber").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Activating with a nonland on top puts a +1/+1 counter on it")
    void exploresNonland() {
        Card nonland = new DuskLegionZealot();
        addTombRobber(nonland);
        harness.setHand(player1, List.of(new Forest()));

        activateAndResolve();

        assertThat(findPermanent(player1, "Tomb Robber").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addTombRobber(new Forest());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPutRevealedNonlandIntoGraveyard() {
        Card nonland = new DuskLegionZealot();
        addTombRobber(nonland);
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonland);
        assertThat(findPermanent(player1, "Tomb Robber").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void exploringEmptyLibraryStillAddsCounter() {
        addTombRobber(new Forest());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Forest()));

        activateAndResolve();

        assertThat(findPermanent(player1, "Tomb Robber").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void paysDiscardBeforeExploringAndCanActivateAgainWhileTapped() {
        Card nonland = new DuskLegionZealot();
        Card firstDiscard = new Forest();
        Card secondDiscard = new Forest();
        addTombRobber(nonland);
        findPermanent(player1, "Tomb Robber").tap();
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDiscard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDiscard);
        assertThat(findPermanent(player1, "Tomb Robber").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        activateAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDiscard, secondDiscard);
        assertThat(findPermanent(player1, "Tomb Robber").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void stillExploresAfterSourceLeavesBattlefield() {
        Card land = new Forest();
        addTombRobber(land);
        harness.setHand(player1, List.of(new DuskLegionZealot()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
    @Test
    void menaceRequiresAtLeastTwoBlockers() {
        addCreatureReady(player1, new TombRobber());
        addCreatureReady(player2, new DuskLegionZealot());
        addCreatureReady(player2, new DuskLegionZealot());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }
    private void addTombRobber(Card topLibraryCard) {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new TombRobber());
        harness.setLibrary(player1, List.of(topLibraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }
    }

}
