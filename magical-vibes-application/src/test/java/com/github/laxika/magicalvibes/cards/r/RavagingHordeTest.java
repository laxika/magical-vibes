package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavagingHorde.class, Mountain.class, ForestBear.class})
class RavagingHordeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target land")
    void etbDestroysTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        castRavagingHorde(land.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertOnBattlefield(player1, "Ravaging Horde");
    }

    @Test
    @DisplayName("ETB can destroy a land its controller controls")
    void etbCanDestroyOwnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        castRavagingHorde(land.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Ravaging Horde");
    }

    @Test
    @DisplayName("Cannot target a non-land creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ForestBear());

        assertThatThrownBy(() -> castRavagingHorde(creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @Test
    @DisplayName("ETB fizzles if the target land leaves before resolution")
    void etbFizzlesIfTargetLandLeavesBeforeResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        castRavagingHorde(land.getId());

        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertOnBattlefield(player1, "Ravaging Horde");
    }

    @Test
    @DisplayName("Can cast without a target when no land exists")
    void canCastWithoutTargetWhenNoLandExists() {
        castRavagingHorde(null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ravaging Horde");
    }

    private void castRavagingHorde(UUID targetId) {
        harness.setHand(player1, List.of(new RavagingHorde()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        if (targetId == null) {
            harness.castCreature(player1, 0);
        } else {
            harness.castCreature(player1, 0, 0, targetId);
        }
    }
}
