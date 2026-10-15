package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KefnetTheMindful.class, Island.class, Forest.class, GrizzlyBears.class, AxegrinderGiant.class})
class KefnetTheMindfulTest extends BaseCardTest {

    @Test
    @DisplayName("Ability draws a card, then the accepted return bounces the chosen land")
    void drawsThenReturnsChosenLand() {
        harness.addToBattlefield(player1, new KefnetTheMindful());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        UUID islandId = harness.getPermanentId(player1, "Island");
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Card is drawn first, then the "may return a land" prompt is offered to the controller.
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(islandId, forestId);

        harness.handlePermanentChosen(player1, islandId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(forestId))
                .noneMatch(p -> p.getId().equals(islandId));
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Declining the optional return still draws a card and leaves lands in play")
    void decliningKeepsLands() {
        harness.addToBattlefield(player1, new KefnetTheMindful());
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(islandId));
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot attack with fewer than seven cards in hand")
    void cannotAttackBelowSeven() {
        addCreatureReady(player1, new KefnetTheMindful());
        harness.setHand(player1, List.of(new Island(), new Island(), new Island()));

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack with seven or more cards in hand")
    void canAttackAtSeven() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new KefnetTheMindful());
        harness.setHand(player1, List.of(new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island(), new Island()));

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot block with fewer than seven cards in hand")
    void cannotBlockBelowSeven() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new KefnetTheMindful());
        harness.setHand(player1, List.of(new Island(), new Island(), new Island()));

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block with seven or more cards in hand")
    void canBlockAtSeven() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new KefnetTheMindful());
        harness.setHand(player1, List.of(new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island(), new Island()));

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Ability draws even when no lands are controlled and Kefnet is summoning sick")
    void drawsWithoutLands() {
        harness.addToBattlefield(player1, new KefnetTheMindful());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Island");

        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Drawing and returning a land can enable attacking by reaching seven cards")
    void drawAndReturnEnableAttacking() {
        addCreatureReady(player1, new KefnetTheMindful());
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, islandId);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);

        harness.setLife(player2, 20);
        declareAttackers(player1, List.of(0));
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Six cards remain insufficient even when the opponent has seven cards")
    void opponentsHandDoesNotEnableAttacking() {
        addCreatureReady(player1, new KefnetTheMindful());
        harness.setHand(player1, List.of(new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A controlled land owned by the opponent returns to its owner's hand")
    void returnsLandToOwnerAndExcludesOpponentsLands() {
        harness.addToBattlefield(player1, new KefnetTheMindful());
        var stolenIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        gd.stolenCreatures.put(stolenIsland.getId(), player2.getId());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(stolenIsland.getId());
        harness.handlePermanentChosen(player1, stolenIsland.getId());

        harness.assertInHand(player1, "Forest");
        harness.assertNotInHand(player1, "Island");
        harness.assertInHand(player2, "Island");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Forest");
    }
}
