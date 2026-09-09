package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinGeneral.class, GoblinPiker.class, GoldenBear.class})
class GoblinGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Goblin General boosts Goblins you control including itself")
    void attackBoostsGoblins() {
        Permanent general = addCreatureReady(player1, new GoblinGeneral());

        Permanent otherGoblin = addCreatureReady(player1, new GoblinPiker());
        Permanent opponentGoblin = addCreatureReady(player2, new GoblinPiker());

        // Give player2 a playable card to prevent auto-pass.
        harness.setHand(player2, List.of(new GoblinPiker()));
        harness.addMana(player2, ManaColor.RED, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        // Goblin General is a Goblin, so it boosts itself too
        assertThat(general.getPowerModifier()).isEqualTo(1);
        assertThat(general.getToughnessModifier()).isEqualTo(1);
        assertThat(otherGoblin.getPowerModifier()).isEqualTo(1);
        assertThat(otherGoblin.getToughnessModifier()).isEqualTo(1);
        assertThat(opponentGoblin.getPowerModifier()).isEqualTo(0);
        assertThat(opponentGoblin.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking with Goblin General does not boost non-Goblin creatures")
    void attackDoesNotBoostNonGoblins() {
        Permanent general = addCreatureReady(player1, new GoblinGeneral());

        Permanent bear = addCreatureReady(player1, new GoldenBear());

        harness.setHand(player2, List.of(new GoblinPiker()));
        harness.addMana(player2, ManaColor.RED, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking with another Goblin does not trigger Goblin General")
    void attackingAnotherGoblinDoesNotTriggerAbility() {
        Permanent general = addCreatureReady(player1, new GoblinGeneral());
        Permanent otherGoblin = addCreatureReady(player1, new GoblinPiker());

        harness.setHand(player2, List.of(new GoblinPiker()));
        harness.addMana(player2, ManaColor.RED, 2);

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(general.getPowerModifier()).isEqualTo(0);
        assertThat(general.getToughnessModifier()).isEqualTo(0);
        assertThat(otherGoblin.getPowerModifier()).isEqualTo(0);
        assertThat(otherGoblin.getToughnessModifier()).isEqualTo(0);
    }
}
