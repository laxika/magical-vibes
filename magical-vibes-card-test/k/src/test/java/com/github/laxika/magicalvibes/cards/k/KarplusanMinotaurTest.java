package com.github.laxika.magicalvibes.cards.k;

import java.util.UUID;
import java.util.ArrayList;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarplusanMinotaur.class})
class KarplusanMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("Cumulative upkeep adds an age counter and may be declined")
    void cumulativeUpkeepMayBeDeclined() {
        var minotaur = harness.addToBattlefieldAndReturn(player1, new KarplusanMinotaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(minotaur.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Karplusan Minotaur");
    }

    @Test
    @DisplayName("Cumulative upkeep flips one coin for each age counter")
    void cumulativeUpkeepFlipsOncePerAgeCounter() {
        var minotaur = harness.addToBattlefieldAndReturn(player1, new KarplusanMinotaur());
        minotaur.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(minotaur.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);

        for (int i = 0; i < 2; i++) {
            var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).as("coin-flip trigger %s", i + 1).isNotNull();
            var chooser = choice.playerId().equals(player1.getId()) ? player1 : player2;
            harness.handlePermanentChosen(chooser, player2.getId());
        }
        resolveAllTriggers();

        assertThat(gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains("coin flip for Karplusan Minotaur")))
                .hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The coin-flip trigger deals damage to a target chosen by the appropriate player")
    void coinFlipTriggerUsesTheCorrectChooser() {
        harness.addToBattlefield(player1, new KarplusanMinotaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        boolean won = gameLogContains("wins the coin flip for Karplusan Minotaur");
        var chooser = won ? player1 : player2;
        var target = won ? player2 : player1;
        harness.handlePermanentChosen(chooser, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(target.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A flip triggers every Minotaur its player controls, but not opposing Minotaurs")
    void coinFlipTriggersOtherControlledMinotaursAndCanDamageCreatures() {
        harness.addToBattlefield(player1, new KarplusanMinotaur());
        advanceToUpkeep(player1);

        harness.addToBattlefield(player1, new KarplusanMinotaur());
        var target = harness.addToBattlefieldAndReturn(player2, new KarplusanMinotaur());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        boolean won = gameLogContains("wins the coin flip for Karplusan Minotaur");
        var chooser = won ? player1 : player2;
        for (int i = 0; i < 2; i++) {
            var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.playerId()).isEqualTo(chooser.getId());
            harness.handlePermanentChosen(chooser, target.getId());
        }
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Karplusan Minotaur");
    }

    @Test
    @DisplayName("Coin-flip targeting follows the controller when the second player controls the Minotaur")
    void coinFlipTriggerWorksForSecondPlayer() {
        harness.addToBattlefield(player2, new KarplusanMinotaur());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        boolean won = gameLogContains("wins the coin flip for Karplusan Minotaur");
        var chooser = won ? player2 : player1;
        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(chooser.getId());
        harness.handlePermanentChosen(chooser, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Karplusan Minotaur");
    }

    private Player addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        Player player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
        return player3;
    }

    @Test
    @DisplayName("A lost flip's target is chosen by the opponent its controller chooses")
    void lostFlipControllerChoosesTheOpponentWhoChoosesTheTarget() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player1, new KarplusanMinotaur());

        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkControllerLosesCoinFlipTriggers(gd, player1.getId());
            harness.getTriggerCollectionService().processNextSpellTargetTrigger(gd);
        });

        PendingInteraction.PermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentChoice.playerId()).isEqualTo(player1.getId());
        assertThat(opponentChoice.validPlayerIds()).containsExactlyInAnyOrder(player2.getId(), player3.getId());
        harness.handlePermanentChosen(player1, player3.getId());

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.playerId()).isEqualTo(player3.getId());
        harness.handlePermanentChosen(player3, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.assertLife(player3, 20);
    }
}
