package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevourInFlames.class, ElspethKnightErrant.class, GrizzlyBears.class, Mountain.class})
class DevourInFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as an additional cost and deals 5 damage to a creature")
    void returnsLandAndDealsDamageToCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevourInFlames()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), land.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mountain");
        harness.assertInGraveyard(player1, "Devour in Flames");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 5 damage to a planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        target.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new DevourInFlames()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), land.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Cannot pay the additional cost with a nonland permanent")
    void cannotReturnNonlandPermanent() {
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevourInFlames()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), nonland.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonland);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent costLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new DevourInFlames()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), costLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped land is returned during casting, before damage resolves")
    void returnsTappedLandBeforeResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        land.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevourInFlames()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), land.getId());

        harness.assertInHand(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotInGraveyard(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot cast without returning a land")
    void cannotCastWithoutReturningLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevourInFlames()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Devour in Flames");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot return an opponent's land as the additional cost")
    void cannotReturnOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevourInFlames()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertInHand(player1, "Devour in Flames");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A controlled land owned by another player returns to its owner's hand")
    void returnsLandToOwner() {
        Mountain borrowedLand = new Mountain();
        borrowedLand.setOwnerId(player2.getId());
        Permanent land = harness.addToBattlefieldAndReturn(player1, borrowedLand);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevourInFlames()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), land.getId());

        harness.assertInHand(player2, "Mountain");
        harness.assertNotInHand(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Mountain");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new DevourInFlames()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, player2.getId(), land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertInHand(player1, "Devour in Flames");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The land remains returned when the target leaves before resolution")
    void additionalCostIsNotRefundedWhenTargetLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevourInFlames()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), land.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Devour in Flames");
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
