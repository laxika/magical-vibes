package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelesnyaSagittars.class, BorosRecruit.class, CourierHawk.class})
class SelesnyaSagittarsTest extends BaseCardTest {

    @Test
    @DisplayName("Selesnya Sagittars can block two attackers")
    void canBlockTwoAttackers() {
        Permanent sagittars = addSagittars();
        int sagittarsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(sagittars);
        addAttackers(2);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(sagittarsIndex, 0),
                new BlockerAssignment(sagittarsIndex, 1)
        ));

        assertThat(sagittars.isBlocking()).isTrue();
        assertThat(sagittars.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Selesnya Sagittars cannot block three attackers")
    void cannotBlockThreeAttackers() {
        Permanent sagittars = addSagittars();
        int sagittarsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(sagittars);
        addAttackers(3);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(sagittarsIndex, 0),
                new BlockerAssignment(sagittarsIndex, 1),
                new BlockerAssignment(sagittarsIndex, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Selesnya Sagittars can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent sagittars = addSagittars();
        int sagittarsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(sagittars);
        Permanent flyer = addCreatureReady(player1, new CourierHawk());
        flyer.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(sagittarsIndex, 0)));

        assertThat(sagittars.isBlocking()).isTrue();
        assertThat(sagittars.getBlockingTargets()).containsExactly(0);
    }

    @Test
    @DisplayName("Selesnya Sagittars does not grant additional blocks to other creatures")
    void doesNotGrantAdditionalBlocksToOthers() {
        addSagittars();

        Permanent otherBlocker = addCreatureReady(player2, new BorosRecruit());
        int otherBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(otherBlocker);

        addAttackers(2);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(otherBlockerIndex, 0),
                new BlockerAssignment(otherBlockerIndex, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Selesnya Sagittars can block two flying attackers")
    void canBlockTwoFlyingAttackers() {
        Permanent sagittars = addSagittars();
        int sagittarsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(sagittars);
        for (int i = 0; i < 2; i++) {
            Permanent flyer = addCreatureReady(player1, new CourierHawk());
            flyer.setAttacking(true);
        }
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(sagittarsIndex, 0),
                new BlockerAssignment(sagittarsIndex, 1)
        ));

        assertThat(sagittars.isBlocking()).isTrue();
        assertThat(sagittars.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("A second Selesnya Sagittars does not increase the first one's blocking limit")
    void secondSagittarsDoesNotIncreaseBlockingLimit() {
        Permanent sagittars = addSagittars();
        int sagittarsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(sagittars);
        addSagittars();
        addAttackers(3);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(sagittarsIndex, 0),
                new BlockerAssignment(sagittarsIndex, 1),
                new BlockerAssignment(sagittarsIndex, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    private Permanent addSagittars() {
        return addCreatureReady(player2, new SelesnyaSagittars());
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent attacker = addCreatureReady(player1, new BorosRecruit());
            attacker.setAttacking(true);
        }
    }
}
