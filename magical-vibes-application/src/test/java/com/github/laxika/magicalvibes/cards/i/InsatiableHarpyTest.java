package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InsatiableHarpy.class, BronzeSable.class})
class InsatiableHarpyTest extends BaseCardTest {

    @Test
    @DisplayName("Insatiable Harpy's lifelink gains life from combat damage")
    void lifelinkGainsLifeOnAttack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new InsatiableHarpy());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Insatiable Harpy cannot be blocked by a creature without flying or reach")
    void flyingCannotBeBlockedByGroundCreature() {
        Permanent harpy = addCreatureReady(player1, new InsatiableHarpy());
        harpy.setAttacking(true);
        addCreatureReady(player2, new BronzeSable());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Flying creatures can block the Harpy and both dying Harpies gain life")
    void flyingBlockerAndAttackerGainLifeDespiteLethalDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new InsatiableHarpy());
        addCreatureReady(player2, new InsatiableHarpy());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        harness.assertInGraveyard(player1, "Insatiable Harpy");
        harness.assertInGraveyard(player2, "Insatiable Harpy");
        harness.assertNotOnBattlefield(player1, "Insatiable Harpy");
        harness.assertNotOnBattlefield(player2, "Insatiable Harpy");
    }
}
