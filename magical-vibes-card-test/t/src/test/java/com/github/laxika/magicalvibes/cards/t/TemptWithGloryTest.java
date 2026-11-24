package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.v.VorinclexMonstrousRaider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemptWithGlory.class, GrizzlyBears.class, Plains.class, VorinclexMonstrousRaider.class})
class TemptWithGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on the controller's creatures and rewards an accepting opponent")
    void acceptingOpponentGetsCountersAndRewardsController() {
        Permanent ownFirst = addCreatureReady(player1);
        Permanent ownSecond = addCreatureReady(player1);
        Permanent opponentFirst = addCreatureReady(player2);
        Permanent opponentSecond = addCreatureReady(player2);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(ownFirst.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ownSecond.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentFirst.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentSecond.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A declining opponent does not receive or grant the extra counters")
    void decliningOpponentDoesNotGetExtraCounters() {
        Permanent own = addCreatureReady(player1);
        Permanent opponent = addCreatureReady(player2);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent with no creatures may accept and still reward the controller")
    void emptyOpponentBattlefieldCanAccept() {
        Permanent own = addCreatureReady(player1);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent can receive counters when the controller has no creatures")
    void emptyControllerBattlefieldDoesNotPreventOffer() {
        Permanent opponent = addCreatureReady(player2);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Noncreature permanents do not receive counters")
    void noncreaturesAreExcluded() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent own = addCreatureReady(player1);
        Permanent opponent = addCreatureReady(player2);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(ownLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting opponents place their own counters for Vorinclex's replacement effects")
    void acceptingOpponentPlacesTheirOwnCounters() {
        Permanent own = addCreatureReady(player1);
        Permanent vorinclex = addCreatureReady(player2, new VorinclexMonstrousRaider());
        Permanent opponent = addCreatureReady(player2);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(vorinclex.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("No opponent puts counters until every opponent has decided, then the controller gets one per acceptor")
    void opponentsDecideBeforeAnyCountersArePut() {
        Player player3 = addThirdPlayer();
        Permanent own = addCreatureReady(player1);
        Permanent second = addCreatureReady(player2);
        Permanent third = addCreatureReady(player3);
        harness.setHand(player1, List.of(new TemptWithGlory()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 6);
        harness.castSorcery(player1, 0);
        passUntilAwaitingInput(player3);

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player3, true);

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("A declining opponent gets no counters while an accepting one does, and only acceptors reward you")
    void onlyAcceptingOpponentsCount() {
        Player player3 = addThirdPlayer();
        Permanent own = addCreatureReady(player1);
        Permanent second = addCreatureReady(player2);
        Permanent third = addCreatureReady(player3);
        harness.setHand(player1, List.of(new TemptWithGlory()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 6);
        harness.castSorcery(player1, 0);
        passUntilAwaitingInput(player3);

        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player3, true);

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void passUntilAwaitingInput(Player player3) {
        for (int i = 0; i < 20 && !gd.interaction.isAwaitingInput(); i++) {
            UUID priorityId = gqs.getPriorityPlayerId(gd);
            Player priorityPlayer = List.of(player1, player2, player3).stream()
                    .filter(player -> player.getId().equals(priorityId)).findFirst().orElseThrow();
            harness.passPriority(priorityPlayer);
        }
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

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void castTemptWithGlory() {
        harness.castFromHand(player1, new TemptWithGlory(), "{5}{W}");
        harness.passBothPriorities();
    }
}
