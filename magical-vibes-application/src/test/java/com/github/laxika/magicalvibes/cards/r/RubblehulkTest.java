package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RubblehulkTest extends BaseCardTest {

    private void addLands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, i % 2 == 0 ? new Forest() : new Mountain());
        }
    }

    private Permanent attackingBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        return bears;
    }

    @Test
    @DisplayName("Rubblehulk's power and toughness equal the number of lands its controller controls")
    void powerToughnessEqualLandCount() {
        addLands(4);
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new Rubblehulk());

        assertThat(gqs.getEffectivePower(gd, hulk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hulk)).isEqualTo(4);

        addLands(2);

        assertThat(gqs.getEffectivePower(gd, hulk)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, hulk)).isEqualTo(6);
    }

    @Test
    @DisplayName("Bloodrush gives target attacking creature +X/+X where X is the land count")
    void bloodrushBoostsByLandCount() {
        addLands(5);
        harness.setHand(player1, List.of(new Rubblehulk()));
        Permanent bears = attackingBears();

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
        harness.assertInGraveyard(player1, "Rubblehulk");
    }

    @Test
    @DisplayName("The bloodrush boost wears off at end of turn")
    void bloodrushWearsOff() {
        addLands(5);
        harness.setHand(player1, List.of(new Rubblehulk()));
        Permanent bears = attackingBears();

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bloodrush cannot target a creature that isn't attacking")
    void bloodrushRejectsNonAttackingCreature() {
        addLands(5);
        harness.setHand(player1, List.of(new Rubblehulk()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Rubblehulk");
    }
}
