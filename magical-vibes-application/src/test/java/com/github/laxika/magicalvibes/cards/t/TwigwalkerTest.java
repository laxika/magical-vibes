package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TwigwalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and gives two target creatures +2/+2")
    void sacrificesSelfAndBoostsTwoCreatures() {
        harness.addToBattlefield(player1, new Twigwalker());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstTarget.getId(), secondTarget.getId()));
        harness.assertNotOnBattlefield(player1, "Twigwalker");
        harness.assertInGraveyard(player1, "Twigwalker");

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstTarget)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstTarget)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondTarget)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondTarget)).isEqualTo(4);
    }

    @Test
    @DisplayName("The two-creature boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Twigwalker());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstTarget)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstTarget)).isEqualTo(4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstTarget)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstTarget)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondTarget)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondTarget)).isEqualTo(2);
    }

    @Test
    @DisplayName("Requires exactly two creature targets")
    void requiresExactlyTwoCreatureTargets() {
        harness.addToBattlefield(player1, new Twigwalker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(UUID.randomUUID())))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
