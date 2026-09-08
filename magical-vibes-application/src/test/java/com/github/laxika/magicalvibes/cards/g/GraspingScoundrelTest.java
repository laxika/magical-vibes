package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraspingScoundrelTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 while attacking")
    void getsPowerBoostWhileAttacking() {
        Permanent scoundrel = addCreatureReady(player1, new GraspingScoundrel());

        assertThat(gqs.getEffectivePower(gd, scoundrel)).isEqualTo(1);

        scoundrel.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, scoundrel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loses the power boost when it stops attacking")
    void losesPowerBoostWhenNotAttacking() {
        Permanent scoundrel = addCreatureReady(player1, new GraspingScoundrel());
        scoundrel.setAttacking(true);

        scoundrel.setAttacking(false);

        assertThat(gqs.getEffectivePower(gd, scoundrel)).isEqualTo(1);
    }
}
