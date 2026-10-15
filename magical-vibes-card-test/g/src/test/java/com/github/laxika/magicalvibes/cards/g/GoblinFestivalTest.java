package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.cards.r.Rescue;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinFestival.class, MetathranSoldier.class, Rescue.class})
class GoblinFestivalTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage before the flip and transfers on a loss")
    void flipsForDamageOrControl() {
        harness.addToBattlefield(player1, new GoblinFestival());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip for Goblin Festival");
        boolean lost = gameLogContains("loses the coin flip for Goblin Festival");
        assertThat(won).isNotEqualTo(lost);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        if (won) {
            harness.assertOnBattlefield(player1, "Goblin Festival");
        } else {
            harness.assertOnBattlefield(player2, "Goblin Festival");
            harness.assertNotOnBattlefield(player1, "Goblin Festival");
        }
    }

    @Test
    @DisplayName("Can deal damage to a permanent as an any-target choice")
    void dealsDamageToPermanent() {
        harness.addToBattlefield(player1, new GoblinFestival());
        harness.addToBattlefield(player2, new MetathranSoldier());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Metathran Soldier"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Metathran Soldier");
    }

    @Test
    @DisplayName("Can damage the player who activated the ability")
    void canTargetActivator() {
        harness.addToBattlefield(player1, new GoblinFestival());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 1);
        assertThat(gameLogContains("coin flip for Goblin Festival")).isTrue();
    }

    @Test
    @DisplayName("Stacked activations still resolve for their activator after any control change")
    void stackedActivationsResolveIndependently() {
        harness.addToBattlefield(player1, new GoblinFestival());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, lifeBefore - 2);
        assertThat(gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains("coin flip for Goblin Festival")))
                .hasSize(2);
        if (gameLogContains("loses the coin flip for Goblin Festival")) {
            harness.assertOnBattlefield(player2, "Goblin Festival");
            harness.assertNotOnBattlefield(player1, "Goblin Festival");
        } else {
            harness.assertOnBattlefield(player1, "Goblin Festival");
            harness.assertNotOnBattlefield(player2, "Goblin Festival");
        }
    }

    @Test
    @DisplayName("An ability still deals damage and flips after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new GoblinFestival());
        harness.setHand(player1, List.of(new Rescue()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Goblin Festival"));
        harness.assertInHand(player1, "Goblin Festival");
        harness.assertNotOnBattlefield(player1, "Goblin Festival");
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 1);
        assertThat(gameLogContains("coin flip for Goblin Festival")).isTrue();
        harness.assertInHand(player1, "Goblin Festival");
        harness.assertNotOnBattlefield(player2, "Goblin Festival");
    }

    @Test
    @DisplayName("No coin is flipped when the only target has become illegal")
    void illegalTargetPreventsEntireAbilityResolving() {
        harness.addToBattlefield(player1, new GoblinFestival());
        harness.addToBattlefield(player2, new MetathranSoldier());
        harness.setHand(player2, List.of(new Rescue()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        var targetId = harness.getPermanentId(player2, "Metathran Soldier");

        harness.activateAbility(player1, 0, null, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Metathran Soldier");
        harness.assertNotInGraveyard(player2, "Metathran Soldier");
        assertThat(gameLogContains("coin flip for Goblin Festival")).isFalse();
        harness.assertOnBattlefield(player1, "Goblin Festival");
        harness.assertNotOnBattlefield(player2, "Goblin Festival");
    }
}
