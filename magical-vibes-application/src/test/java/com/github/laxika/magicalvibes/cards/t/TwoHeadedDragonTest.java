package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TwoHeadedDragonTest extends BaseCardTest {

    // ===== {1}{R}: +2/+0 pump =====

    @Test
    @DisplayName("Activating ability gives +2/+0")
    void activatingAbilityBoostsPower() {
        Permanent dragon = addReadyDragon(player1);
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
        Permanent dragon = addReadyDragon(player1);
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
        Permanent dragon = addReadyDragon(player1);
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
        TwoHeadedDragon card = new TwoHeadedDragon();
        Permanent dragonPerm = new Permanent(card);
        dragonPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(dragonPerm);
        int dragonIdx = gd.playerBattlefields.get(player2.getId()).indexOf(dragonPerm);

        addAttacker(player1);
        addAttacker(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

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
        TwoHeadedDragon card = new TwoHeadedDragon();
        Permanent dragonPerm = new Permanent(card);
        dragonPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(dragonPerm);
        int dragonIdx = gd.playerBattlefields.get(player2.getId()).indexOf(dragonPerm);

        for (int i = 0; i < 3; i++) {
            addAttacker(player1);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(dragonIdx, 0),
                new BlockerAssignment(dragonIdx, 1),
                new BlockerAssignment(dragonIdx, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    private void addAttacker(Player attacker) {
        GrizzlyBears atk = new GrizzlyBears();
        Permanent atkPerm = new Permanent(atk);
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(attacker.getId()).add(atkPerm);
    }

    private Permanent addReadyDragon(Player player) {
        TwoHeadedDragon card = new TwoHeadedDragon();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
