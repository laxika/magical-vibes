package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GerrardsBattleCryTest extends BaseCardTest {

    @Test
    @DisplayName("Activation gives creatures you control +1/+1 and stacks across activations")
    void pumpsOwnCreatures() {
        addBattleCry();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 6);

        activateAndResolve();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);

        activateAndResolve();

        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's creatures are unaffected")
    void opponentCreaturesUnaffected() {
        addBattleCry();
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        activateAndResolve();

        assertThat(enemyBears.getPowerModifier()).isEqualTo(0);
        assertThat(enemyBears.getToughnessModifier()).isEqualTo(0);
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void addBattleCry() {
        Permanent perm = new Permanent(new GerrardsBattleCry());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
    }
}
