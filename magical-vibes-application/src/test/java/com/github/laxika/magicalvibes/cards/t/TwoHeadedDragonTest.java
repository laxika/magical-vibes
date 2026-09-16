package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwoHeadedDragon.class, GrizzlyBears.class, AirElemental.class})
class TwoHeadedDragonTest extends BaseCardTest {

    // ===== {1}{R}: +2/+0 pump =====

    @Test
    @DisplayName("Activating ability gives +2/+0")
    void activatingAbilityBoostsPower() {
        Permanent dragon = addCreatureReady(player1, new TwoHeadedDragon());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(2);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Pump can be activated repeatedly, stacking +2/+0")
    void pumpStacks() {
        Permanent dragon = addCreatureReady(player1, new TwoHeadedDragon());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(dragon.getPowerModifier()).isEqualTo(4);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Pump wears off at end of turn")
    void pumpWearsOff() {
        Permanent dragon = addCreatureReady(player1, new TwoHeadedDragon());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(dragon.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(0);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Can block an additional creature =====

    @Test
    @DisplayName("Two-Headed Dragon can block two attackers")
    void canBlockTwoAttackers() {
        Permanent dragonPerm = addCreatureReady(player2, new TwoHeadedDragon());
        int dragonIdx = gd.playerBattlefields.get(player2.getId()).indexOf(dragonPerm);

        addAttacker(player1);
        addAttacker(player1);

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(dragonIdx, 0),
                new BlockerAssignment(dragonIdx, 1)
        ));

        assertThat(dragonPerm.isBlocking()).isTrue();
        assertThat(dragonPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Two-Headed Dragon cannot block three attackers")
    void cannotBlockThreeAttackers() {
        Permanent dragonPerm = addCreatureReady(player2, new TwoHeadedDragon());
        int dragonIdx = gd.playerBattlefields.get(player2.getId()).indexOf(dragonPerm);

        for (int i = 0; i < 3; i++) {
            addAttacker(player1);
        }

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(dragonIdx, 0),
                new BlockerAssignment(dragonIdx, 1),
                new BlockerAssignment(dragonIdx, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent dragon = addCreatureReady(player1, new TwoHeadedDragon());
        dragon.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Menace prevents blocking with only one flying creature")
    void menaceRequiresTwoBlockers() {
        Permanent dragon = addCreatureReady(player1, new TwoHeadedDragon());
        dragon.setAttacking(true);
        addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("except by two or more creatures");
    }

    @Test
    @DisplayName("Menace allows two flying creatures to block")
    void menaceAllowsTwoBlockers() {
        Permanent dragon = addCreatureReady(player1, new TwoHeadedDragon());
        dragon.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new AirElemental());
        Permanent secondBlocker = addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
        assertThat(firstBlocker.getBlockingTargets()).containsExactly(0);
        assertThat(secondBlocker.getBlockingTargets()).containsExactly(0);
    }

    private void addAttacker(Player attacker) {
        Permanent atkPerm = addCreatureReady(attacker, new GrizzlyBears());
        atkPerm.setAttacking(true);
    }
}
