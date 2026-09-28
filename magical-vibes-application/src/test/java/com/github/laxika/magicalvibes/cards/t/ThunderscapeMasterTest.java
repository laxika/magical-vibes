package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderscapeMaster.class, ThornscapeApprentice.class, Forest.class})
class ThunderscapeMasterTest extends BaseCardTest {

    @Test
    @DisplayName("{B}{B}, {T}: target player loses 2 life and you gain 2 life")
    void drainsTargetPlayer() {
        addReadyMaster();
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The life-drain ability only targets players")
    void drainAbilityRejectsPermanentTarget() {
        addReadyMaster();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThornscapeApprentice());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{G}{G}, {T}: creatures you control get +2/+2 until end of turn")
    void boostsOwnCreatures() {
        Permanent master = addReadyMaster();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ThornscapeApprentice());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ThornscapeApprentice());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(master.getPowerModifier()).isEqualTo(2);
        assertThat(master.getToughnessModifier()).isEqualTo(2);
        assertThat(ownCreature.getPowerModifier()).isEqualTo(2);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(2);
        assertThat(opposingCreature.getPowerModifier()).isEqualTo(0);
        assertThat(opposingCreature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The creature boost does not affect noncreatures you control")
    void boostLeavesOwnNoncreaturesUnchanged() {
        addReadyMaster();
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ownLand.getPowerModifier()).isEqualTo(0);
        assertThat(ownLand.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The creature boost wears off at cleanup")
    void boostWearsOff() {
        addReadyMaster();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ThornscapeApprentice());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(0);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The tap cost prevents activating another ability until the source untaps")
    void tapCostPreventsSecondActivation() {
        Permanent master = addReadyMaster();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(master.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMaster() {
        return addCreatureReady(player1, new ThunderscapeMaster());
    }
}
