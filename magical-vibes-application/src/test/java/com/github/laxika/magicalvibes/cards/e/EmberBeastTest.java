package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberBeast.class, WoodlandDruid.class})
class EmberBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Ember Beast can't attack alone")
    void cantAttackAlone() {
        addCreatureReady(player1, new EmberBeast());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ember Beast can attack with another creature")
    void canAttackWithAnother() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new EmberBeast());
        addCreatureReady(player1, new WoodlandDruid());

        declareAttackers(List.of(0, 1));

        // Ember Beast (3/4) + Woodland Druid (1/2) = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Ember Beast can't block alone")
    void cantBlockAlone() {
        Permanent attacker = addCreatureReady(player1, new WoodlandDruid());
        attacker.setAttacking(true);

        addCreatureReady(player2, new EmberBeast());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ember Beast can block with another creature")
    void canBlockWithAnother() {
        Permanent attacker1 = addCreatureReady(player1, new WoodlandDruid());
        attacker1.setAttacking(true);

        Permanent attacker2 = addCreatureReady(player1, new WoodlandDruid());
        attacker2.setAttacking(true);

        Permanent beast = addCreatureReady(player2, new EmberBeast());

        Permanent druid = addCreatureReady(player2, new WoodlandDruid());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(beast.isBlocking()).isTrue();
        assertThat(druid.isBlocking()).isTrue();
    }
}
