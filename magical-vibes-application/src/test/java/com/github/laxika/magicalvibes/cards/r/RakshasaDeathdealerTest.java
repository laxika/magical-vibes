package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RakshasaDeathdealerTest extends BaseCardTest {

    @Test
    @DisplayName("{B}{G}: gets +2/+2 until end of turn")
    void pumpAbilityBoostsPowerAndToughness() {
        Permanent deathdealer = addCreatureReady(player1, new RakshasaDeathdealer());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(deathdealer.getPowerModifier()).isEqualTo(2);
        assertThat(deathdealer.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("{B}{G}: regenerates")
    void regenerationAbilityGrantsShield() {
        Permanent deathdealer = addCreatureReady(player1, new RakshasaDeathdealer());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(deathdealer.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Power and toughness boost wears off at end of turn")
    void pumpAbilityWearsOffAtEndOfTurn() {
        Permanent deathdealer = addCreatureReady(player1, new RakshasaDeathdealer());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(deathdealer.getPowerModifier()).isEqualTo(0);
        assertThat(deathdealer.getToughnessModifier()).isEqualTo(0);
    }
}
