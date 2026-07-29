package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinSoothsayerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability boosts red creatures on both sides but not non-red creatures")
    void boostsRedCreaturesOnly() {
        addCreatureReady(player1, new GoblinSoothsayer());
        Permanent ownGiant = addCreatureReady(player1, new HillGiant());
        Permanent enemyGiant = addCreatureReady(player2, new HillGiant());
        Permanent enemyBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownGiant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownGiant)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, enemyGiant)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, enemyBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new GoblinSoothsayer());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing itself as the Goblin still resolves the boost")
    void canSacrificeItself() {
        addCreatureReady(player1, new GoblinSoothsayer());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Soothsayer");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate without red mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new GoblinSoothsayer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
