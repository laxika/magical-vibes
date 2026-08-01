package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        List<Permanent> islands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .toList();

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

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
