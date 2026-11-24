package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshnodsCylix;
import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Misfortune.class, ElvishRanger.class, AshnodsCylix.class})
class MisfortuneTest extends BaseCardTest {

    private static final String FIRST_MODE = "You put a +1/+1 counter on each creature you control and gain 4 life";
    private static final String SECOND_MODE =
            "You put a -1/-1 counter on each creature that player controls and Misfortune deals 4 damage to that player";

    @Test
    @DisplayName("Casting prompts the opponent to choose a mode before priority is passed")
    void castingPromptsOpponentChoice() {
        setupAndCast();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Accept: +1/+1 counter on each creature the controller controls and they gain 4 life")
    void acceptGrowsControllerCreaturesAndGainsLife() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new ElvishRanger());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        setupAndCast();
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.handleListChoice(player2, FIRST_MODE);
        harness.passBothPriorities();

        assertThat(mine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(theirs.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Decline: -1/-1 counter on each of that player's creatures and 4 damage to them")
    void declineShrinksOpponentCreaturesAndBurnsThem() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new ElvishRanger());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        setupAndCast();
        GameData gd = harness.getGameData();
        int controllerLife = gd.playerLifeTotals.get(player1.getId());

        harness.handleListChoice(player2, SECOND_MODE);
        harness.passBothPriorities();

        assertThat(theirs.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(mine.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife);
    }

    @Test
    @DisplayName("The first mode puts counters only on creatures")
    void acceptIgnoresNoncreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishRanger());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AshnodsCylix());
        setupAndCast();

        harness.handleListChoice(player2, FIRST_MODE);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The second mode puts counters only on creatures")
    void declineIgnoresNoncreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AshnodsCylix());
        setupAndCast();

        harness.handleListChoice(player2, SECOND_MODE);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The first mode gains 4 life with no creatures")
    void acceptGainsLifeWithNoCreatures() {
        setupAndCast();
        chooseModeAndFinishOriginalResolution(true);

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The second mode deals 4 damage with no creatures")
    void declineDealsDamageWithNoCreatures() {
        setupAndCast();
        chooseModeAndFinishOriginalResolution(false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The first mode puts a counter on every creature the controller controls")
    void acceptCountersEveryControlledCreatureWithinOriginalResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ElvishRanger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ElvishRanger());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        setupAndCast();
        chooseModeAndFinishOriginalResolution(true);

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("The second mode kills that player's 1-toughness creatures and deals 4 damage")
    void declineKillsEveryOneToughnessCreatureAndDealsDamageWithinOriginalResolution() {
        harness.addToBattlefield(player2, new ElvishRanger());
        harness.addToBattlefield(player2, new ElvishRanger());
        harness.addToBattlefield(player1, new ElvishRanger());
        setupAndCast();
        chooseModeAndFinishOriginalResolution(false);

        assertThat(countPermanents(player2, "Elvish Ranger")).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof ElvishRanger).hasSize(2);
        harness.assertOnBattlefield(player1, "Elvish Ranger");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("With two opponents, the controller picks the opponent who chooses the mode")
    void controllerPicksTheChoosingOpponent() {
        Player player3 = addThirdPlayer();
        setupAndCast();

        PendingInteraction.PermanentChoice pick = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(pick.playerId()).isEqualTo(player1.getId());
        assertThat(pick.validPlayerIds()).containsExactlyInAnyOrder(player2.getId(), player3.getId());

        harness.handlePermanentChosen(player1, player3.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player3.getId());
    }

    @Test
    @DisplayName("The second mode shrinks and burns only the opponent who chose it")
    void secondModeHitsOnlyTheChoosingOpponent() {
        Player player3 = addThirdPlayer();
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        Permanent third = harness.addToBattlefieldAndReturn(player3, new ElvishRanger());
        setupAndCast();
        harness.handlePermanentChosen(player1, player3.getId());
        harness.handleListChoice(player3, SECOND_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player3.getId())).doesNotContain(third);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertLife(player3, 16);
        harness.assertLife(player2, 20);
    }

    private void chooseModeAndFinishOriginalResolution(boolean accepted) {
        boolean choosingDuringCasting = gd.interaction.isAwaitingInput();
        if (!choosingDuringCasting) {
            harness.passBothPriorities();
        }
        harness.handleListChoice(player2, accepted ? FIRST_MODE : SECOND_MODE);
        if (choosingDuringCasting) {
            harness.passBothPriorities();
        }
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new Misfortune(), "{1}{B}{R}{G}");
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
