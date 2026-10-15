package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemptingContract.class})
class TemptingContractTest extends BaseCardTest {

    @Test
    void acceptedOpponentCreatesTreasureAndControllerCreatesTreasure() {
        harness.addToBattlefield(player1, new TemptingContract());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
    }

    @Test
    void declinedOpponentCreatesNoTreasure() {
        harness.addToBattlefield(player1, new TemptingContract());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new TemptingContract());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void triggeredAbilityStillResolvesAfterContractLeavesBattlefield() {
        var contract = harness.addToBattlefieldAndReturn(player1, new TemptingContract());
        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(contract);
        gd.playerGraveyards.get(player1.getId()).add(contract.getCard());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
    }

    @Test
    void allOpponentsChooseBeforeAnyTreasuresAreCreated() {
        harness.addToBattlefield(player1, new TemptingContract());
        advanceToUpkeep(player1);
        Player player3 = addThirdPlayer();
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.passPriority(player1);
            harness.passPriority(player2);
            harness.passPriority(player3);
        });

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
        assertThat(countPermanents(player3, "Treasure")).isZero();

        harness.handleMayAbilityChosen(player3, true);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player3, "Treasure")).isEqualTo(1);
    }

    private Player addThirdPlayer() {
        UUID id = UUID.randomUUID();
        Player player = new Player(id, "Charlie");
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), id, "Charlie");
        return player;
    }
}
