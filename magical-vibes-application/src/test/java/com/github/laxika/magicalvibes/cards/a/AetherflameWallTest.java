package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LooterIlKor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherflameWall.class, LooterIlKor.class, AshcoatBear.class})
class AetherflameWallTest extends BaseCardTest {

    @Test
    @DisplayName("Aetherflame Wall can block a creature with shadow")
    void blocksShadowAttacker() {
        Permanent wall = addCreatureReady(player2, new AetherflameWall());
        Permanent attacker = addCreatureReady(player1, new LooterIlKor());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Aetherflame Wall still blocks a creature without shadow")
    void blocksNormalAttacker() {
        Permanent wall = addCreatureReady(player2, new AetherflameWall());
        Permanent attacker = addCreatureReady(player1, new AshcoatBear());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without Aetherflame Wall's ability cannot block a creature with shadow")
    void plainBlockerCannotBlockShadow() {
        addCreatureReady(player2, new AshcoatBear());
        Permanent attacker = addCreatureReady(player1, new LooterIlKor());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Aetherflame Wall gets +1/+0 until end of turn")
    void firebreathingBoostsPower() {
        Permanent wall = addCreatureReady(player1, new AetherflameWall());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
    }

    @Test
    @DisplayName("Aetherflame Wall's boost wears off at end of turn")
    void firebreathingBoostWearsOff() {
        Permanent wall = addCreatureReady(player1, new AetherflameWall());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
    }

}
