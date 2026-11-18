package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeatherbackBaloth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TombHex.class, Forest.class, HillGiant.class, LeatherbackBaloth.class})
class TombHexTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -2/-2 without landfall")
    void givesMinusTwoMinusTwoWithoutLandfall() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castTombHex(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives target creature -4/-4 after landfall")
    void givesMinusFourMinusFourWithLandfall() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Forest(), new TombHex()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TombHex()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Landfall replaces the normal reduction and expires at end of turn")
    void landfallReplacesNormalReductionAndExpires() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        castTombHex(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Leatherback Baloth");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent's land entering does not enable landfall")
    void opponentsLandDoesNotEnableLandfall() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.enterBattlefieldAndReturn(player2, new Forest());
        castTombHex(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Landfall is checked at resolution, not when casting")
    void landEnteringBeforeResolutionEnablesLandfall() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new TombHex()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple lands entering do not increase the landfall reduction")
    void multipleLandsDoNotIncreaseReduction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        castTombHex(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("A land entering after resolution does not upgrade the reduction")
    void landEnteringAfterResolutionDoesNotUpgradeReduction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeatherbackBaloth());
        castTombHex(creature);
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A land from the previous turn does not enable landfall and the normal reduction expires")
    void previousTurnsLandDoesNotEnableLandfall() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeatherbackBaloth());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        castTombHex(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    private void castTombHex(Permanent target) {
        harness.setHand(player1, List.of(new TombHex()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
