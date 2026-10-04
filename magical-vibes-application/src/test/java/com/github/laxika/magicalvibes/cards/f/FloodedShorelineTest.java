package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FloodedShoreline.class, GrizzlyBears.class, Island.class, Plains.class})
class FloodedShorelineTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two Islands as cost and bounces target creature")
    void returnsIslandsAndBouncesCreature() {
        harness.addToBattlefield(player1, new FloodedShoreline());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        int shorelineIndex = battlefieldIndex(player1, "Flooded Shoreline");
        harness.activateAbility(player1, shorelineIndex, null, bears.getId());

        // Exactly two Islands → auto-returned as cost
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Island"))
                .hasSize(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Flooded Shoreline");
    }

    @Test
    @DisplayName("Cannot activate without two Islands")
    void cannotActivateWithoutTwoIslands() {
        harness.addToBattlefield(player1, new FloodedShoreline());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        int shorelineIndex = battlefieldIndex(player1, "Flooded Shoreline");
        UUID targetId = bears.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, shorelineIndex, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new FloodedShoreline());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        int shorelineIndex = battlefieldIndex(player1, "Flooded Shoreline");
        UUID targetId = bears.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, shorelineIndex, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new FloodedShoreline());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Plains());
        UUID targetId = findPermanent(player2, "Plains").getId();
        harness.addMana(player1, ManaColor.BLUE, 2);

        int shorelineIndex = battlefieldIndex(player1, "Flooded Shoreline");
        assertThatThrownBy(() -> harness.activateAbility(player1, shorelineIndex, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chooses which Islands to return when more than two are available")
    void choosesIslandsWhenMoreThanTwo() {
        harness.addToBattlefield(player1, new FloodedShoreline());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        List<Permanent> islands = findPermanents(player1, "Island");

        int shorelineIndex = battlefieldIndex(player1, "Flooded Shoreline");
        harness.activateAbility(player1, shorelineIndex, null, bears.getId());

        assertThat(gd.stack).isEmpty();

        harness.handlePermanentChosen(player1, islands.get(0).getId());
        harness.handlePermanentChosen(player1, islands.get(1).getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Island"))
                .hasSize(1);

        harness.passBothPriorities();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tapped Islands can pay the return cost and your own creature can be targeted")
    void returnsTappedIslandsAndOwnCreature() {
        harness.addToBattlefield(player1, new FloodedShoreline());
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        firstIsland.tap();
        secondIsland.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Flooded Shoreline"), null, bears.getId());

        assertThat(countPermanents(player1, "Island")).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent's Islands cannot pay the activation cost")
    void cannotUseOpponentsIslands() {
        harness.addToBattlefield(player1, new FloodedShoreline());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                battlefieldIndex(player1, "Flooded Shoreline"), null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents");

        assertThat(countPermanents(player1, "Island")).isEqualTo(1);
        assertThat(countPermanents(player2, "Island")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returned Islands and a controlled creature go to their owners' hands")
    void returnsBorrowedPermanentsToOwners() {
        harness.addToBattlefield(player1, new FloodedShoreline());
        Permanent borrowedIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent borrowedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(borrowedIsland.getId(), player2.getId());
        gd.stolenCreatures.put(borrowedBears.getId(), player2.getId());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Flooded Shoreline"), null,
                borrowedBears.getId());

        harness.assertInHand(player1, "Island");
        harness.assertInHand(player2, "Island");
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
