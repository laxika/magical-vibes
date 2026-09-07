package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrosanGroundshaker.class, KrosanBeast.class, GrizzlyBears.class})
class KrosanGroundshakerTest extends BaseCardTest {

    @Test
    @DisplayName("Grants trample to a target Beast creature")
    void grantsTrampleToBeast() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent beast = addCreatureReady(player1, new KrosanBeast());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, beast.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The granted trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent beast = addCreatureReady(player1, new KrosanBeast());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, beast.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Beast creature")
    void cannotTargetNonBeastCreature() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Beast creature");
    }
}
