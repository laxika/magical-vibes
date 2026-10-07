package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExoticOrchard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormOfSteel.class, GrizzlyBears.class, ExoticOrchard.class})
class StormOfSteelTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each of two targets")
    void dealsDamageToTwoTargets() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castStormOfSteel(List.of(firstBear.getId(), secondBear.getId()));

        assertThat(firstBear.getMarkedDamage()).isEqualTo(2);
        assertThat(secondBear.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can deal 2 damage to one target")
    void dealsDamageToOneTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castStormOfSteel(List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(untargeted.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Requires at least one target")
    void requiresAtLeastOneTarget() {
        harness.setHand(player1, List.of(new StormOfSteel()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals the full amount to both players")
    void dealsDamageToBothPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castStormOfSteel(List.of(player1.getId(), player2.getId()));

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Can target a creature and a player together")
    void dealsDamageToCreatureAndPlayer() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        castStormOfSteel(List.of(bear.getId(), player2.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot choose the same target twice")
    void rejectsDuplicateTargets() {
        harness.setHand(player1, List.of(new StormOfSteel()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose more than two targets")
    void rejectsThreeTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StormOfSteel()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player1.getId(), player2.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still damages the remaining target when one target leaves")
    void resolvesWithOneRemainingTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new StormOfSteel()));
        addMana();
        harness.castSorcery(player1, 0, List.of(bear.getId(), player2.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(bear);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Storm of Steel");
    }

    @Test
    @DisplayName("Cannot target a noncreature land")
    void rejectsNoncreatureLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new ExoticOrchard());
        harness.setHand(player1, List.of(new StormOfSteel()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not resolve when all targets have left the battlefield")
    void doesNotResolveWithNoRemainingTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StormOfSteel()));
        addMana();
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));

        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(first, second));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Storm of Steel");
    }
    private void castStormOfSteel(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new StormOfSteel()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
