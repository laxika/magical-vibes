package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinSettler.class, Forest.class, GrizzlyBears.class})
class GoblinSettlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys the target land")
    void etbDestroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new GoblinSettler()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID landId = harness.getPermanentId(player2, "Forest");
        harness.castCreature(player1, 0, landId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("ETB trigger targets the chosen land")
    void etbTriggerTargetsLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new GoblinSettler()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID landId = harness.getPermanentId(player2, "Forest");
        harness.castCreature(player1, 0, landId);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Goblin Settler");
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(landId);
    }

    @Test
    @DisplayName("Only lands are legal targets for the ETB")
    void onlyLandsAreLegalTargets() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoblinSettler()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        harness.castCreature(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @Test
    @DisplayName("Can cast with no target when no lands exist")
    void canCastWithoutTargetWhenNoLands() {
        harness.setHand(player1, List.of(new GoblinSettler()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("ETB can destroy your own land without affecting another land")
    void etbCanDestroyOwnLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new GoblinSettler()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID landId = harness.getPermanentId(player1, "Forest");
        harness.castCreature(player1, 0, landId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Goblin Settler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature resolves with no lands and leaves no target interaction")
    void resolvesWithoutLands() {
        harness.setHand(player1, List.of(new GoblinSettler()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Settler");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
