package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import java.util.List;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutumnWillowTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent cannot target Autumn Willow while it has shroud")
    void opponentCannotTargetWithShroud() {
        harness.addToBattlefield(player1, new AutumnWillow());
        Permanent willow = findPermanent(player1, "Autumn Willow");

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, willow.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("After the ability resolves the targeted player may target Autumn Willow")
    void targetedPlayerMayTarget() {
        harness.addToBattlefield(player1, new AutumnWillow());
        Permanent willow = findPermanent(player1, "Autumn Willow");
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(willow.ignoresShroudFor(player2.getId())).isTrue();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        gs.playCard(gd, player2, 0, 0, willow.getId(), null);
        harness.passBothPriorities();

        assertThat(willow.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the targeted player gains the permission")
    void permissionIsPerPlayer() {
        harness.addToBattlefield(player1, new AutumnWillow());
        Permanent willow = findPermanent(player1, "Autumn Willow");
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(willow.ignoresShroudFor(player1.getId())).isTrue();
        assertThat(willow.ignoresShroudFor(player2.getId())).isFalse();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, willow.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("The permission wears off at end of turn")
    void permissionWearsOff() {
        harness.addToBattlefield(player1, new AutumnWillow());
        Permanent willow = findPermanent(player1, "Autumn Willow");
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(willow.ignoresShroudFor(player2.getId())).isTrue();

        willow.resetModifiers();

        assertThat(willow.ignoresShroudFor(player2.getId())).isFalse();
    }
}
