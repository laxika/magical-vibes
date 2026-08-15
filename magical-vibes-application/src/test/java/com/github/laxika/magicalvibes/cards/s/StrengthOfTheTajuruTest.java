package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrengthOfTheTajuruTest extends BaseCardTest {

    @Test
    @DisplayName("Without multikicker, puts X counters on one target creature")
    void putsXCountersOnOneTargetWithoutMultikicker() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 4); // {1}{G}{G}

        castWithTargets(1, List.of(bears.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each multikicker payment adds another target and each target gets X counters")
    void multikickerAddsTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 6); // {2}{G}{G} + {1} + {1}
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        castWithTargets(3, List.of(bears.getId(), giant.getId()), List.of("{1}", "{1}"));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot target more creatures than the number of targets bought by multikicker")
    void cannotTargetMoreCreaturesThanBought() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 4); // {1}{G}{G}

        List<UUID> targets = List.of(bears.getId(), giant.getId());
        assertThatThrownBy(() -> castWithTargets(1, targets, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 4); // {1}{G}{G}

        assertThatThrownBy(() -> castWithTargets(1, List.of(artifact.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWithTargets(int xValue, List<UUID> targets, List<String> repeatedCosts) {
        gs.playCard(gd, player1, 0, xValue, null, null, targets, List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                repeatedCosts, false);
    }
}
