package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeadshotMinotaurTest extends BaseCardTest {

    // ===== ETB deals 3 damage to a creature with flying =====

    @Test
    @DisplayName("ETB deals 3 damage to target creature with flying")
    void etbDeals3DamageToFlyer() {
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        addManaCost(player1);

        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);

        // Resolve creature spell → ETB triggers, then resolve ETB.
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent flyer = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Air Elemental"))
                .findFirst().orElseThrow();
        assertThat(flyer.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB kills a 1/1 flyer")
    void etbKillsSmallFlyer() {
        harness.addToBattlefield(player2, new SuntailHawk());
        UUID targetId = harness.getPermanentId(player2, "Suntail Hawk");

        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        addManaCost(player1);

        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        addManaCost(player1);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cycling {R/G} =====

    @Test
    @DisplayName("Cycling discards the card and draws one, paid with red")
    void cyclingDrawsACardWithRed() {
        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Deadshot Minotaur");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling can be paid with green")
    void cyclingDrawsACardWithGreen() {
        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Deadshot Minotaur");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void addManaCost(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 4);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
