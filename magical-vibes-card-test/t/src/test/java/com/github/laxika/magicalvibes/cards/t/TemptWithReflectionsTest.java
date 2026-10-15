package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BalefulStrix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemptWithReflections.class, GrizzlyBears.class, BalefulStrix.class})
class TemptWithReflectionsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a copy for the controller and rewards an accepting opponent")
    void acceptingOpponentCreatesCopiesForBothPlayers() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castTemptWithReflections(harness.getPermanentId(player1, "Grizzly Bears"));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(3);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player1, "Grizzly Bears")).filteredOn(p -> p.getCard().isToken())
                .hasSize(2);
        assertThat(findPermanents(player2, "Grizzly Bears")).filteredOn(p -> p.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("A declining opponent does not receive or grant an additional copy")
    void decliningOpponentDoesNotCreateAdditionalCopies() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castTemptWithReflections(harness.getPermanentId(player1, "Grizzly Bears"));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TemptWithReflections()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copies do not inherit tapped status or counters and retain enters abilities")
    void copiesRetainEntersAbilitiesWithoutCountersOrTappedStatus() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalefulStrix());
        target.tap();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLibrary(player1, List.of(new BalefulStrix(), new BalefulStrix()));
        harness.setLibrary(player2, List.of(new BalefulStrix()));

        castTemptWithReflections(target.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Baleful Strix")).hasSize(3);
        assertThat(findPermanents(player2, "Baleful Strix")).hasSize(1);
        for (Player player : List.of(player1, player2)) {
            assertThat(findPermanents(player, "Baleful Strix"))
                    .filteredOn(permanent -> permanent.getCard().isToken())
                    .allSatisfy(permanent -> {
                        assertThat(permanent.isTapped()).isFalse();
                        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                    });
        }
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A target that leaves before resolution prevents every copy and the offer")
    void missingTargetPreventsCopiesAndOffer() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TemptWithReflections()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Tempt with Reflections");
    }

    @Test
    @DisplayName("No accepting opponent receives a copy until all opponents have chosen")
    void opponentsReceiveCopiesOnlyAfterAllChoices() {
        UUID id = UUID.randomUUID();
        Player player3 = new Player(id, "Charlie");
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
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalefulStrix());
        for (Player player : List.of(player1, player2, player3)) {
            harness.setLibrary(player, List.of(new BalefulStrix(), new BalefulStrix(), new BalefulStrix()));
        }
        harness.setHand(player1, List.of(new TemptWithReflections()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, target.getId());
        for (int i = 0; i < 20 && !gd.interaction.isAwaitingInput(); i++) {
            UUID priorityId = gqs.getPriorityPlayerId(gd);
            Player priorityPlayer = List.of(player1, player2, player3).stream()
                    .filter(player -> player.getId().equals(priorityId)).findFirst().orElseThrow();
            harness.passPriority(priorityPlayer);
        }
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(findPermanents(player1, "Baleful Strix")).hasSize(2);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(findPermanents(player2, "Baleful Strix")).isEmpty();
        assertThat(findPermanents(player1, "Baleful Strix")).hasSize(2);

        harness.handleMayAbilityChosen(player3, true);

        assertThat(findPermanents(player1, "Baleful Strix")).hasSize(4);
        assertThat(findPermanents(player2, "Baleful Strix")).hasSize(1);
        assertThat(findPermanents(player3, "Baleful Strix")).hasSize(1);
    }

    @Test
    @DisplayName("An existing creature token can be copied by the tempting offer")
    void copiesAnExistingCreatureToken() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castTemptWithReflections(original.getId());
        harness.handleMayAbilityChosen(player2, false);
        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        castTemptWithReflections(token.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(4);
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(3);
        assertThat(findPermanents(player2, "Grizzly Bears"))
                .singleElement().satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
    }

    private void castTemptWithReflections(UUID targetId) {
        harness.setHand(player1, List.of(new TemptWithReflections()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
