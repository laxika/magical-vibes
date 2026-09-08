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

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
